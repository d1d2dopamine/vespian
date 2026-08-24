package dev.trial3lib.ui.compat

import dev.trial3lib.ui.graphic.Trial3Glyph

/*
 * The icon set, under Material's names.
 *
 * Material ships thousands of filled vectors; a real app uses thirty of them and
 * drags the whole artifact along for the ride. This object answers the names a
 * migrated app already writes -- Icons.Filled.Alarm, Icons.AutoMirrored.Filled.ArrowBack --
 * with Trial3 marks, so no icon dependency is left on the classpath.
 *
 * Where Material had a mark Trial3 does not draw, the nearest honest mark is
 * used and said so in a comment. Nothing here invents a rounded shape.
 */
public object Icons {

    /** Material's filled set. The only set: Trial3 draws one weight. */
    public object Filled {
        public val Add: Trial3Glyph get() = Trial3Glyph.PLUS
        public val Remove: Trial3Glyph get() = Trial3Glyph.MINUS
        public val Close: Trial3Glyph get() = Trial3Glyph.CLOSE
        public val Check: Trial3Glyph get() = Trial3Glyph.CHECK
        public val CheckCircle: Trial3Glyph get() = Trial3Glyph.CHECK_CIRCLE
        public val RadioButtonUnchecked: Trial3Glyph get() = Trial3Glyph.CIRCLE
        public val ArrowBack: Trial3Glyph get() = Trial3Glyph.ARROW_LEFT
        public val ArrowForward: Trial3Glyph get() = Trial3Glyph.ARROW_RIGHT
        public val ExpandMore: Trial3Glyph get() = Trial3Glyph.CHEVRON_DOWN
        public val ExpandLess: Trial3Glyph get() = Trial3Glyph.CHEVRON_UP
        public val Menu: Trial3Glyph get() = Trial3Glyph.MENU
        public val Search: Trial3Glyph get() = Trial3Glyph.SEARCH
        public val Settings: Trial3Glyph get() = Trial3Glyph.SETTINGS

        /** Material's sliders-in-a-row. */
        public val Tune: Trial3Glyph get() = Trial3Glyph.SLIDERS
        public val Edit: Trial3Glyph get() = Trial3Glyph.EDIT
        public val Delete: Trial3Glyph get() = Trial3Glyph.TRASH
        public val Download: Trial3Glyph get() = Trial3Glyph.DOWNLOAD
        public val Upload: Trial3Glyph get() = Trial3Glyph.UPLOAD
        public val Share: Trial3Glyph get() = Trial3Glyph.SHARE
        public val ContentCopy: Trial3Glyph get() = Trial3Glyph.COPY
        public val Send: Trial3Glyph get() = Trial3Glyph.SEND
        public val Folder: Trial3Glyph get() = Trial3Glyph.FOLDER
        public val Description: Trial3Glyph get() = Trial3Glyph.FILE
        public val Layers: Trial3Glyph get() = Trial3Glyph.LAYERS
        public val Sync: Trial3Glyph get() = Trial3Glyph.SYNC

        /** Refresh and Sync are the same two arrows in Trial3. */
        public val Refresh: Trial3Glyph get() = Trial3Glyph.SYNC
        public val Timeline: Trial3Glyph get() = Trial3Glyph.CHART
        public val Schedule: Trial3Glyph get() = Trial3Glyph.CLOCK
        public val Alarm: Trial3Glyph get() = Trial3Glyph.ALARM
        public val Bedtime: Trial3Glyph get() = Trial3Glyph.MOON
        public val LightMode: Trial3Glyph get() = Trial3Glyph.SUN
        public val Hotel: Trial3Glyph get() = Trial3Glyph.BED
        public val Favorite: Trial3Glyph get() = Trial3Glyph.HEART
        public val MonitorHeart: Trial3Glyph get() = Trial3Glyph.PULSE
        public val LocalCafe: Trial3Glyph get() = Trial3Glyph.CUP
        public val Science: Trial3Glyph get() = Trial3Glyph.FLASK
        public val BatteryFull: Trial3Glyph get() = Trial3Glyph.BATTERY
        public val Bluetooth: Trial3Glyph get() = Trial3Glyph.BLUETOOTH
        public val Notifications: Trial3Glyph get() = Trial3Glyph.BELL
        public val Warning: Trial3Glyph get() = Trial3Glyph.WARNING

        /** Material draws Error as a filled circle; the triangle carries it here. */
        public val Error: Trial3Glyph get() = Trial3Glyph.WARNING
        public val Info: Trial3Glyph get() = Trial3Glyph.CIRCLE
        public val Key: Trial3Glyph get() = Trial3Glyph.KEY
        public val Smartphone: Trial3Glyph get() = Trial3Glyph.PHONE
        public val PlayArrow: Trial3Glyph get() = Trial3Glyph.PLAY
        public val HealthAndSafety: Trial3Glyph get() = Trial3Glyph.SHIELD

        /** A guided tour: the flag you are walked to. */
        public val Tour: Trial3Glyph get() = Trial3Glyph.FLAG
    }

    /** Material's alias for the filled set. */
    public val Default: Filled get() = Filled

    /**
     * Marks that flip in a right-to-left layout. Trial3 draws them from lines,
     * so the mirroring is the layout's job, not a second asset's.
     */
    public object AutoMirrored {
        public object Filled {
            public val ArrowBack: Trial3Glyph get() = Trial3Glyph.ARROW_LEFT
            public val ArrowForward: Trial3Glyph get() = Trial3Glyph.ARROW_RIGHT
            public val Send: Trial3Glyph get() = Trial3Glyph.SEND
        }

        public val Default: Filled get() = Filled
    }
}
