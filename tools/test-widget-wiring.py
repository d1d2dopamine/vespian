#!/usr/bin/env python3
"""Static checks for provider boundaries omitted by the JVM harness."""
from pathlib import Path
s=(Path(__file__).resolve().parents[1]/'app/src/main/java/dev/vespian/work/ForecastWidget.kt').read_text()
render=s.split('private fun render(',1)[1].split('private fun createViews',1)[0]
assert render.index('if (ids.isEmpty()) return') < render.index('val pending = goAsync()')
assert render.count('pending.finish()') == 1
assert 'finally {\n                pending.finish()' in render
assert render.count('throw cancelled') == 2
assert 'runCatching' not in s
assert 'catch (_: Exception)' in render and 'catch (_: Throwable)' not in s
assert 'ForecastPresentation.canPresent(forecast.nights)' in s
assert 'context.sendBroadcast(intent)' in s
assert 'PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT' in s
print('PASS: empty IDs guard, single finish in finally, cancellation propagation, bounded exception catches, zero-night guard, existing refresh broadcast/click intent')
