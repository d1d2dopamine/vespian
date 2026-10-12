package probe
import dev.vespian.model.ForecastPresentation
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
class ColdHarness {
 private val context=Context()
 @Before fun reset() { Engine.value=null;Engine.throwForecast=false;Engine.calls=0;Engine.onCall=null;sent.clear();Db.dao.rows.clear() }
 @Test fun helperRejectsNonpositiveOnly() {
  for(n in listOf(Int.MIN_VALUE,-1,0)) assertFalse(ForecastPresentation.canPresent(n))
  for(n in listOf(1,2,7,Int.MAX_VALUE)) assertTrue(ForecastPresentation.canPresent(n))
 }
 @Test fun commandSuppressesNoNights() = runBlocking {
  for(n in listOf(0,-1)) { Engine.value=Forecast(n);assertNull(Commands.forecastText(context)) }
 }
 @Test fun whyExplainsUnavailableWithoutTimes() = runBlocking {
  for(n in listOf(0,-1)) { sent.clear();Engine.value=Forecast(n);Commands.why(context);assertEquals(listOf(context.getString(R.string.tgb_no_forecast)),sent) }
 }
 @Test fun widgetClearsAllPriorTimes() = runBlocking {
  for(n in listOf(0,-1)) { Engine.value=Forecast(n);val v=Widget.build(context).values;assertEquals(context.getString(R.string.widget_none),v[R.id.widget_value]);assertEquals("",v[R.id.widget_range]);assertEquals("",v[R.id.widget_gate]) }
 }
 @Test fun eveningDoesNotEnqueueOrConsumeDay() = runBlocking {
  for(n in listOf(0,-1)) { Engine.value=Forecast(n);Bot.maybeEvening(context);assertTrue(sent.isEmpty());assertTrue(Db.dao.rows.isEmpty()); }
 }
 @Test fun eveningRecheckAlsoSuppressesCountChange() = runBlocking {
  Engine.value=Forecast(1);Engine.onCall={if(it==2) Engine.value=Forecast(0)}
  Bot.maybeEvening(context);assertTrue(sent.isEmpty());assertTrue(Db.dao.rows.isEmpty())
 }
 @Test fun oneNightPreservesExistingOutputsWithoutValidationClaim() = runBlocking {
  Engine.value=Forecast(1);val text=Commands.forecastText(context)!!;assertTrue(text.contains("TIME[22.0]"));assertTrue(text.contains("tg_evening_reverse"))
  Commands.why(context);assertTrue(sent.single().contains("tgb_why_head"));sent.clear()
  assertTrue(Widget.build(context).values[R.id.widget_value]!!.contains("TIME[22.0]"))
  Bot.maybeEvening(context);assertEquals(listOf(text),sent);assertEquals(1,Db.dao.rows.size)
 }
 @Test fun telegramFailureRemainsUnavailableAndSilentAutomatically() = runBlocking {
  Engine.throwForecast=true;assertNull(Commands.forecastText(context));Commands.why(context);assertFalse(sent.single().contains("TIME["));sent.clear();Bot.maybeEvening(context);assertTrue(sent.isEmpty());assertTrue(Db.dao.rows.isEmpty())
 }
}
