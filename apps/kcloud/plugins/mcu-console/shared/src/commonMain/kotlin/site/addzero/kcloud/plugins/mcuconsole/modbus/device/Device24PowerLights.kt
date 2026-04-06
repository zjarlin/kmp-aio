package site.addzero.kcloud.plugins.mcuconsole.modbus.device

/**
 * 24 路电源灯状态快照。
 */
data class Device24PowerLights(
    val light1: Boolean,
    val light2: Boolean,
    val light3: Boolean,
    val light4: Boolean,
    val light5: Boolean,
    val light6: Boolean,
    val light7: Boolean,
    val light8: Boolean,
    val light9: Boolean,
    val light10: Boolean,
    val light11: Boolean,
    val light12: Boolean,
    val light13: Boolean,
    val light14: Boolean,
    val light15: Boolean,
    val light16: Boolean,
    val light17: Boolean,
    val light18: Boolean,
    val light19: Boolean,
    val light20: Boolean,
    val light21: Boolean,
    val light22: Boolean,
    val light23: Boolean,
    val light24: Boolean,
) {
    fun asList(): List<Boolean> {
        return listOf(
            light1,
            light2,
            light3,
            light4,
            light5,
            light6,
            light7,
            light8,
            light9,
            light10,
            light11,
            light12,
            light13,
            light14,
            light15,
            light16,
            light17,
            light18,
            light19,
            light20,
            light21,
            light22,
            light23,
            light24,
        )
    }
}
