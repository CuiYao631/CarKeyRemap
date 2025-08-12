package com.example.carkeyremap.model

/**
 * 按键映射配置数据类
 */
data class KeyMapping(
    val id: String = "",
    val sourceKeyCode: Int,
    val sourceKeyName: String,
    val actionType: ActionType,
    val targetKeyCode: Int? = null,
    val targetKeyName: String? = null,
    val clickX: Int? = null,
    val clickY: Int? = null,
    val isEnabled: Boolean = true
)

/**
 * 动作类型枚举
 */
enum class ActionType {
    KEY_EVENT,      // 按键事件
    SCREEN_CLICK,   // 屏幕点击
    GESTURE         // 手势
}

/**
 * 常用按键代码映射
 */
object KeyCodes {
    const val VOLUME_UP = 24
    const val VOLUME_DOWN = 25
    const val BACK = 4
    const val HOME = 3
    const val MENU = 82
    const val SEARCH = 84
    const val CALL = 5
    const val ENDCALL = 6
    const val POWER = 26
    const val CAMERA = 27
    const val CLEAR = 28
    const val A = 29
    const val B = 30
    const val C = 31
    const val D = 32
    const val E = 33
    const val F = 34
    const val G = 35
    const val H = 36
    const val I = 37
    const val J = 38
    const val K = 39
    const val L = 40
    const val M = 41
    const val N = 42
    const val O = 43
    const val P = 44
    const val Q = 45
    const val R = 46
    const val S = 47
    const val T = 48
    const val U = 49
    const val V = 50
    const val W = 51
    const val X = 52
    const val Y = 53
    const val Z = 54
    const val COMMA = 55
    const val PERIOD = 56
    const val ALT_LEFT = 57
    const val ALT_RIGHT = 58
    const val SHIFT_LEFT = 59
    const val SHIFT_RIGHT = 60
    const val TAB = 61
    const val SPACE = 62
    const val SYM = 63
    const val EXPLORER = 64
    const val ENVELOPE = 65
    const val ENTER = 66
    const val DEL = 67
    const val GRAVE = 68
    const val MINUS = 69
    const val EQUALS = 70
    const val LEFT_BRACKET = 71
    const val RIGHT_BRACKET = 72
    const val BACKSLASH = 73
    const val SEMICOLON = 74
    const val APOSTROPHE = 75
    const val SLASH = 76
    const val AT = 77
    const val NUM = 78
    const val HEADSETHOOK = 79
    const val FOCUS = 80
    const val PLUS = 81
    const val NOTIFICATION = 83
    const val MEDIA_PLAY_PAUSE = 85
    const val MEDIA_STOP = 86
    const val MEDIA_NEXT = 87
    const val MEDIA_PREVIOUS = 88
    const val MEDIA_REWIND = 89
    const val MEDIA_FAST_FORWARD = 90
    const val MUTE = 91
    const val PAGE_UP = 92
    const val PAGE_DOWN = 93
    const val PICTSYMBOLS = 94
    const val SWITCH_CHARSET = 95
    const val BUTTON_A = 96
    const val BUTTON_B = 97
    const val BUTTON_C = 98
    const val BUTTON_X = 99
    const val BUTTON_Y = 100
    const val BUTTON_Z = 101
    const val BUTTON_L1 = 102
    const val BUTTON_R1 = 103
    const val BUTTON_L2 = 104
    const val BUTTON_R2 = 105
    const val BUTTON_THUMBL = 106
    const val BUTTON_THUMBR = 107
    const val BUTTON_START = 108
    const val BUTTON_SELECT = 109
    const val BUTTON_MODE = 110
    const val ESCAPE = 111
    const val FORWARD_DEL = 112
    const val CTRL_LEFT = 113
    const val CTRL_RIGHT = 114
    const val CAPS_LOCK = 115
    const val SCROLL_LOCK = 116
    const val META_LEFT = 117
    const val META_RIGHT = 118
    const val FUNCTION = 119
    const val SYSRQ = 120
    const val BREAK = 121
    const val MOVE_HOME = 122
    const val MOVE_END = 123
    const val INSERT = 124
    const val FORWARD = 125
    const val MEDIA_PLAY = 126
    const val MEDIA_PAUSE = 127
    const val MEDIA_CLOSE = 128
    const val MEDIA_EJECT = 129
    const val MEDIA_RECORD = 130
    const val F1 = 131
    const val F2 = 132
    const val F3 = 133
    const val F4 = 134
    const val F5 = 135
    const val F6 = 136
    const val F7 = 137
    const val F8 = 138
    const val F9 = 139
    const val F10 = 140
    const val F11 = 141
    const val F12 = 142
    const val NUM_LOCK = 143
    const val NUMPAD_0 = 144
    const val NUMPAD_1 = 145
    const val NUMPAD_2 = 146
    const val NUMPAD_3 = 147
    const val NUMPAD_4 = 148
    const val NUMPAD_5 = 149
    const val NUMPAD_6 = 150
    const val NUMPAD_7 = 151
    const val NUMPAD_8 = 152
    const val NUMPAD_9 = 153
    const val NUMPAD_DIVIDE = 154
    const val NUMPAD_MULTIPLY = 155
    const val NUMPAD_SUBTRACT = 156
    const val NUMPAD_ADD = 157
    const val NUMPAD_DOT = 158
    const val NUMPAD_COMMA = 159
    const val NUMPAD_ENTER = 160
    const val NUMPAD_EQUALS = 161
    const val NUMPAD_LEFT_PAREN = 162
    const val NUMPAD_RIGHT_PAREN = 163
    const val VOLUME_MUTE = 164
    const val INFO = 165
    const val CHANNEL_UP = 166
    const val CHANNEL_DOWN = 167
    const val ZOOM_IN = 168
    const val ZOOM_OUT = 169
    const val TV = 170
    const val WINDOW = 171
    const val GUIDE = 172
    const val DVR = 173
    const val BOOKMARK = 174
    const val CAPTIONS = 175
    const val SETTINGS = 176
    const val TV_POWER = 177
    const val TV_INPUT = 178
    const val STB_POWER = 179
    const val STB_INPUT = 180
    const val AVR_POWER = 181
    const val AVR_INPUT = 182
    const val PROG_RED = 183
    const val PROG_GREEN = 184
    const val PROG_YELLOW = 185
    const val PROG_BLUE = 186
    const val APP_SWITCH = 187
    const val BUTTON_1 = 188
    const val BUTTON_2 = 189
    const val BUTTON_3 = 190
    const val BUTTON_4 = 191
    const val BUTTON_5 = 192
    const val BUTTON_6 = 193
    const val BUTTON_7 = 194
    const val BUTTON_8 = 195
    const val BUTTON_9 = 196
    const val BUTTON_10 = 197
    const val BUTTON_11 = 198
    const val BUTTON_12 = 199
    const val BUTTON_13 = 200
    const val BUTTON_14 = 201
    const val BUTTON_15 = 202
    const val BUTTON_16 = 203
    const val LANGUAGE_SWITCH = 204
    const val MANNER_MODE = 205
    const val MODE_3D = 206
    const val CONTACTS = 207
    const val CALENDAR = 208
    const val MUSIC = 209
    const val CALCULATOR = 210
    const val ZENKAKU_HANKAKU = 211
    const val EISU = 212
    const val MUHENKAN = 213
    const val HENKAN = 214
    const val KATAKANA_HIRAGANA = 215
    const val YEN = 216
    const val RO = 217
    const val KANA = 218
    const val ASSIST = 219
    const val BRIGHTNESS_DOWN = 220
    const val BRIGHTNESS_UP = 221
    const val MEDIA_AUDIO_TRACK = 222
    const val SLEEP = 223
    const val WAKEUP = 224
    const val PAIRING = 225
    const val MEDIA_TOP_MENU = 226
    const val KEY_11 = 227
    const val KEY_12 = 228
    const val LAST_CHANNEL = 229
    const val TV_DATA_SERVICE = 230
    const val VOICE_ASSIST = 231
    const val TV_RADIO_SERVICE = 232
    const val TV_TELETEXT = 233
    const val TV_NUMBER_ENTRY = 234
    const val TV_TERRESTRIAL_ANALOG = 235
    const val TV_TERRESTRIAL_DIGITAL = 236
    const val TV_SATELLITE = 237
    const val TV_SATELLITE_BS = 238
    const val TV_SATELLITE_CS = 239
    const val TV_SATELLITE_SERVICE = 240
    const val TV_NETWORK = 241
    const val TV_ANTENNA_CABLE = 242
    const val TV_INPUT_HDMI_1 = 243
    const val TV_INPUT_HDMI_2 = 244
    const val TV_INPUT_HDMI_3 = 245
    const val TV_INPUT_HDMI_4 = 246
    const val TV_INPUT_COMPOSITE_1 = 247
    const val TV_INPUT_COMPOSITE_2 = 248
    const val TV_INPUT_COMPONENT_1 = 249
    const val TV_INPUT_COMPONENT_2 = 250
    const val TV_INPUT_VGA_1 = 251
    const val TV_AUDIO_DESCRIPTION = 252
    const val TV_AUDIO_DESCRIPTION_MIX_UP = 253
    const val TV_AUDIO_DESCRIPTION_MIX_DOWN = 254
    const val TV_ZOOM_MODE = 255
    const val TV_CONTENTS_MENU = 256
    const val TV_MEDIA_CONTEXT_MENU = 257
    const val TV_TIMER_PROGRAMMING = 258
    const val HELP = 259
    const val NAVIGATE_PREVIOUS = 260
    const val NAVIGATE_NEXT = 261
    const val NAVIGATE_IN = 262
    const val NAVIGATE_OUT = 263
    const val STEM_PRIMARY = 264
    const val STEM_1 = 265
    const val STEM_2 = 266
    const val STEM_3 = 267
    const val DPAD_UP_LEFT = 268
    const val DPAD_DOWN_LEFT = 269
    const val DPAD_UP_RIGHT = 270
    const val DPAD_DOWN_RIGHT = 271
    const val MEDIA_SKIP_FORWARD = 272
    const val MEDIA_SKIP_BACKWARD = 273
    const val MEDIA_STEP_FORWARD = 274
    const val MEDIA_STEP_BACKWARD = 275
    const val SOFT_SLEEP = 276
    const val CUT = 277
    const val COPY = 278
    const val PASTE = 279
    const val SYSTEM_NAVIGATION_UP = 280
    const val SYSTEM_NAVIGATION_DOWN = 281
    const val SYSTEM_NAVIGATION_LEFT = 282
    const val SYSTEM_NAVIGATION_RIGHT = 283
    const val ALL_APPS = 284
    const val REFRESH = 285
    const val THUMBS_UP = 286
    const val THUMBS_DOWN = 287
    const val PROFILE_SWITCH = 288
    
    val keyCodeNames = mapOf(
        VOLUME_UP to "音量加键",
        VOLUME_DOWN to "音量减键",
        BACK to "返回键",
        HOME to "主页键",
        MENU to "菜单键",
        SEARCH to "搜索键",
        CALL to "通话键",
        ENDCALL to "挂断键",
        POWER to "电源键",
        CAMERA to "相机键",
        SPACE to "空格键",
        ENTER to "回车键",
        DEL to "删除键",
        TAB to "Tab键",
        MEDIA_PLAY_PAUSE to "播放/暂停键",
        MEDIA_STOP to "停止键",
        MEDIA_NEXT to "下一曲",
        MEDIA_PREVIOUS to "上一曲",
        MUTE to "静音键",
        PAGE_UP to "Page Up",
        PAGE_DOWN to "Page Down",
        ESCAPE to "Esc键",
        F1 to "F1键",
        F2 to "F2键",
        F3 to "F3键",
        F4 to "F4键",
        F5 to "F5键",
        F6 to "F6键",
        F7 to "F7键",
        F8 to "F8键",
        F9 to "F9键",
        F10 to "F10键",
        F11 to "F11键",
        F12 to "F12键",
        CHANNEL_UP to "频道+",
        CHANNEL_DOWN to "频道-",
        ZOOM_IN to "放大",
        ZOOM_OUT to "缩小",
        TV to "TV键",
        GUIDE to "电视指南",
        SETTINGS to "设置键",
        BRIGHTNESS_UP to "亮度+",
        BRIGHTNESS_DOWN to "亮度-",
        ASSIST to "助手键",
        APP_SWITCH to "应用切换",
        CONTACTS to "联系人",
        CALENDAR to "日历",
        MUSIC to "音乐",
        CALCULATOR to "计算器"
    )
    
    fun getKeyName(keyCode: Int): String {
        return keyCodeNames[keyCode] ?: "未知按键($keyCode)"
    }
}
