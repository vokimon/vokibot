package net.canvoki.vokibot.time

import android.content.Context
import android.text.format.DateUtils
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.canvoki.vokibot.EntityMetadata
import net.canvoki.vokibot.FileDataRepository
import net.canvoki.vokibot.JsonConfig
import net.canvoki.vokibot.R
import net.canvoki.vokibot.StorableEntity
import net.canvoki.vokibot.Trigger
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.UUID

@Serializable
enum class Recurrence {
    None,
    Daily,
    Weekly,
}

/**
 * Triggers on time condition
 */
@Serializable
data class TimeTrigger(
    @Serializable(LocalDateTimeIsoSerializer::class)
    val startAt: LocalDateTime,
    val displayName: String,
    override val id: String = UUID.randomUUID().toString(),
    val recurrence: Recurrence,
    @Serializable(WeekdaysSerializer::class)
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    val weekdays: Set<DayOfWeek> = emptySet(),
) : Trigger() {
    override val type: String = typeKey

    // Secondary constructor accepts nullable id.
    // Because primary's id: String rejects nullable,
    // Kotlin resolves all id: String? calls to this secondary.
    // Params reordered (id first) to avoid JVM signature clash.
    constructor(
        id: String?,
        startAt: LocalDateTime,
        displayName: String,
        recurrence: Recurrence,
        weekdays: Set<DayOfWeek> = emptySet(),
    ) : this(
        startAt = startAt,
        displayName = displayName,
        id = id ?: UUID.randomUUID().toString(),
        recurrence = recurrence,
        weekdays = weekdays,
    )

    companion object : EntityMetadata {
        const val DESCRIPTION_FORMAT = "yyyy-MM-dd HH:mm"
        const val TIME_FORMAT = "HH:mm"

        override val typeKey = "trigger_time"
        override val iconRes = R.drawable.ic_schedule
        override val entityClass = TimeTrigger::class
        override val labelRes = R.string.triggerlist_option_time
        override val helpRes = R.string.trigger_time_help

        override val deserializer = { jsonString: String -> fromJson(jsonString) }
        override val editorFactory = { editingId: String? -> TimeTriggerEditor(editingId) }

        fun fromJson(jsonString: String): TimeTrigger = JsonConfig.decodeFromString(serializer(), jsonString)

        fun register() = StorableEntity.register(this)

        fun scheduleAll(context: Context) {
            val repository = FileDataRepository.fromContext(context)
            repository.trigger
                .all()
                .filterIsInstance<TimeTrigger>()
                .forEach { it.schedule(context) }
        }
    }

    override val iconRes: Int get() = TimeTrigger.iconRes

    override fun onRemoved(context: Context) {
        TimeScheduler.cancel(context, id)
    }

    fun nextOccurrence(now: LocalDateTime): LocalDateTime? {
        val candidate = now.toLocalDate().atTime(startAt.toLocalTime())
        return when (recurrence) {
            Recurrence.None -> if (startAt.isAfter(now)) startAt else null
            Recurrence.Daily ->
                when {
                    candidate.isAfter(now) -> candidate
                    else -> candidate.plusDays(1)
                }
            Recurrence.Weekly ->
                when {
                    weekdays.isEmpty() -> null
                    candidate.isAfter(now) && candidate.dayOfWeek in weekdays -> candidate
                    else ->
                        generateSequence(candidate.plusDays(1)) { it.plusDays(1) }
                            .first { it.dayOfWeek in weekdays }
                }
        }
    }

    fun nextOccurrenceHumanized(
        context: Context,
        now: LocalDateTime,
    ): String {
        val occurrence = nextOccurrence(now) ?: return "expired"
        return DateUtils
            .getRelativeTimeSpanString(
                occurrence.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                now.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                DateUtils.MINUTE_IN_MILLIS,
                0,
            ).toString()
    }

    fun schedule(context: Context) {
        val occurrence = nextOccurrence(LocalDateTime.now())
        if (occurrence != null) {
            TimeScheduler.schedule(context, id, occurrence)
        } else {
            TimeScheduler.cancel(context, id)
        }
    }

    override fun getTitle(context: Context): String = displayName.ifBlank { absoluteDescription(context) }

    override fun description(context: Context): String =
        if (displayName.isBlank()) {
            nextOccurrenceHumanized(context, LocalDateTime.now())
        } else {
            listOf(
                absoluteDescription(context),
                nextOccurrenceHumanized(context, LocalDateTime.now()),
            ).joinToString(" | ")
        }

    fun absoluteDescription(context: Context): String =
        when (recurrence) {
            Recurrence.None -> startAt.format(DateTimeFormatter.ofPattern(DESCRIPTION_FORMAT))
            Recurrence.Daily ->
                context.getString(
                    R.string.trigger_time_description_daily,
                    startAt.format(DateTimeFormatter.ofPattern(TIME_FORMAT)),
                )
            Recurrence.Weekly ->
                // TODO: localize; use locale day names; handle none
                {
                    val day =
                        if (weekdays.size == 1) {
                            weekdays
                                .first()
                                .name
                                .lowercase()
                                .replaceFirstChar { it.uppercase() }
                        } else {
                            weekdays
                                .sorted()
                                .joinToString(", ") {
                                    it.name
                                        .lowercase()
                                        .take(3)
                                        .replaceFirstChar { it.uppercase() }
                                }
                        }
                    val time = startAt.format(DateTimeFormatter.ofPattern(TIME_FORMAT))
                    "$day at $time"
                }
        }

    override fun toJson(): String = JsonConfig.encodeToString(serializer(), this)
}
