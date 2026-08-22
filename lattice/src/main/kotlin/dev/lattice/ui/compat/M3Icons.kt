package dev.lattice.ui.compat

import dev.lattice.ui.graphic.LatGlyph

/*
 * The icon set, under Material's names.
 *
 * Material ships thousands of filled vectors; a real app uses thirty of them and
 * drags the whole artifact along for the ride. This object answers the names a
 * migrated app already writes -- Icons.Filled.Alarm, Icons.AutoMirrored.Filled.ArrowBack --
 * with Lattice marks, so no icon dependency is left on the classpath.
 *
 * Where Material had a mark Lattice does not draw, the nearest honest mark is
 * used and said so in a comment. Nothing here invents a rounded shape.
 */
public object Icons {

    /** Material's filled set. The only set: Lattice draws one weight. */
    public object Filled {
        public val Add: LatGlyph get() = LatGlyph.PLUS
        public val Remove: LatGlyph get() = LatGlyph.MINUS
        public val Close: LatGlyph get() = LatGlyph.CLOSE
        public val Check: LatGlyph get() = LatGlyph.CHECK
        public val CheckCircle: LatGlyph get() = LatGlyph.CHECK_CIRCLE
        public val RadioButtonUnchecked: LatGlyph get() = LatGlyph.CIRCLE
        public val ArrowBack: LatGlyph get() = LatGlyph.ARROW_LEFT
        public val ArrowForward: LatGlyph get() = LatGlyph.ARROW_RIGHT
        public val ExpandMore: LatGlyph get() = LatGlyph.CHEVRON_DOWN
        public val ExpandLess: LatGlyph get() = LatGlyph.CHEVRON_UP
        public val Menu: LatGlyph get() = LatGlyph.MENU
        public val Search: LatGlyph get() = LatGlyph.SEARCH
        public val Settings: LatGlyph get() = LatGlyph.SETTINGS

        /** Material's sliders-in-a-row. */
        public val Tune: LatGlyph get() = LatGlyph.SLIDERS
        public val Edit: LatGlyph get() = LatGlyph.EDIT
        public val Delete: LatGlyph get() = LatGlyph.TRASH
        public val Download: LatGlyph get() = LatGlyph.DOWNLOAD
        public val Upload: LatGlyph get() = LatGlyph.UPLOAD
        public val Share: LatGlyph get() = LatGlyph.SHARE
        public val ContentCopy: LatGlyph get() = LatGlyph.COPY
        public val Send: LatGlyph get() = LatGlyph.SEND
        public val Folder: LatGlyph get() = LatGlyph.FOLDER
        public val Description: LatGlyph get() = LatGlyph.FILE
        public val Layers: LatGlyph get() = LatGlyph.LAYERS
        public val Sync: LatGlyph get() = LatGlyph.SYNC

        /** Refresh and Sync are the same two arrows in Lattice. */
        public val Refresh: LatGlyph get() = LatGlyph.SYNC
        public val Timeline: LatGlyph get() = LatGlyph.CHART
        public val Schedule: LatGlyph get() = LatGlyph.CLOCK
        public val Alarm: LatGlyph get() = LatGlyph.ALARM
        public val Bedtime: LatGlyph get() = LatGlyph.MOON
        public val LightMode: LatGlyph get() = LatGlyph.SUN
        public val Hotel: LatGlyph get() = LatGlyph.BED
        public val Favorite: LatGlyph get() = LatGlyph.HEART
        public val MonitorHeart: LatGlyph get() = LatGlyph.PULSE
        public val LocalCafe: LatGlyph get() = LatGlyph.CUP
        public val Science: LatGlyph get() = LatGlyph.FLASK
        public val BatteryFull: LatGlyph get() = LatGlyph.BATTERY
        public val Bluetooth: LatGlyph get() = LatGlyph.BLUETOOTH
        public val Notifications: LatGlyph get() = LatGlyph.BELL
        public val Warning: LatGlyph get() = LatGlyph.WARNING

        /** Material draws Error as a filled circle; the triangle carries it here. */
        public val Error: LatGlyph get() = LatGlyph.WARNING
        public val Info: LatGlyph get() = LatGlyph.CIRCLE
        public val Key: LatGlyph get() = LatGlyph.KEY
        public val Smartphone: LatGlyph get() = LatGlyph.PHONE
        public val PlayArrow: LatGlyph get() = LatGlyph.PLAY
        public val HealthAndSafety: LatGlyph get() = LatGlyph.SHIELD

        /** A guided tour: the flag you are walked to. */
        public val Tour: LatGlyph get() = LatGlyph.FLAG
    }

    /** Material's alias for the filled set. */
    public val Default: Filled get() = Filled

    /**
     * Marks that flip in a right-to-left layout. Lattice draws them from lines,
     * so the mirroring is the layout's job, not a second asset's.
     */
    public object AutoMirrored {
        public object Filled {
            public val ArrowBack: LatGlyph get() = LatGlyph.ARROW_LEFT
            public val ArrowForward: LatGlyph get() = LatGlyph.ARROW_RIGHT
            public val Send: LatGlyph get() = LatGlyph.SEND
        }

        public val Default: Filled get() = Filled
    }
}
