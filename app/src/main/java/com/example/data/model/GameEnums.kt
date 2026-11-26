package com.example.data.model

enum class OfficerSpecialty(
    val titleFa: String,
    val shortDescFa: String
) {
    PATROL("گشت شهری", "تخصص در امنیت محله و مهار سریع درگیری‌ها"),
    TRAFFIC("پلیس راهور", "تخصص در تصادفات، بزرگراه‌ها و کشف خودرو"),
    DETECTIVE("کارآگاه", "تخصص در پرونده‌های پیچیده و یافتن افراد گمشده"),
    TACTICAL("عملیات ویژه", "تخصص در تعقیب مجرمان و مأموریت‌های سنگین"),
    NEGOTIATOR("مذاکره‌کننده", "تخصص در حل مسالمت‌آمیز بحران و نجات گروگان");

    companion object {
        fun fromName(name: String): OfficerSpecialty =
            entries.firstOrNull { it.name == name } ?: PATROL
    }
}

enum class CrimeType(
    val titleFa: String,
    val preferredSpecialty: OfficerSpecialty,
    val minLevel: Int,
    val baseDifficulty: Int,
    val requiredOfficers: Int,
    val baseRequiredPower: Int,
    val baseRewardMoney: Long,
    val baseRewardXp: Int,
    val baseDurationSec: Int,
    val minSuspects: Int,
    val maxSuspects: Int,
    val baseEscapeRisk: Int,
    val needsFastVehicle: Boolean,
    val escapeWindowSeconds: Int,
    val reportTemplates: List<String>
) {
    SHOP_THEFT(
        titleFa = "سرقت از فروشگاه",
        preferredSpecialty = OfficerSpecialty.PATROL,
        minLevel = 1,
        baseDifficulty = 1,
        requiredOfficers = 1,
        baseRequiredPower = 34,
        baseRewardMoney = 150L,
        baseRewardXp = 34,
        baseDurationSec = 16,
        minSuspects = 1,
        maxSuspects = 2,
        baseEscapeRisk = 24,
        needsFastVehicle = true,
        escapeWindowSeconds = 90,
        reportTemplates = listOf(
            "گزارش سرقت از فروشگاه زنجیره‌ای ثبت شده و سارق قصد فرار از درب پشتی دارد!",
            "فروشنده بازارچه از سرقت صندوق و فرار متهم به سمت کوچه‌های اطراف خبر داده است."
        )
    ),
    PUBLIC_DISTURBANCE(
        titleFa = "ایجاد مزاحمت",
        preferredSpecialty = OfficerSpecialty.PATROL,
        minLevel = 1,
        baseDifficulty = 1,
        requiredOfficers = 1,
        baseRequiredPower = 28,
        baseRewardMoney = 125L,
        baseRewardXp = 28,
        baseDurationSec = 14,
        minSuspects = 1,
        maxSuspects = 2,
        baseEscapeRisk = 16,
        needsFastVehicle = false,
        escapeWindowSeconds = 110,
        reportTemplates = listOf(
            "ساکنان محله از سروصدا و ایجاد مزاحمت در میدان خبر داده‌اند.",
            "گزارش ایجاد مزاحمت برای کسبه و عابران پیاده دریافت شده است."
        )
    ),
    TRAFFIC_ACCIDENT(
        titleFa = "تصادف رانندگی",
        preferredSpecialty = OfficerSpecialty.TRAFFIC,
        minLevel = 2,
        baseDifficulty = 2,
        requiredOfficers = 1,
        baseRequiredPower = 50,
        baseRewardMoney = 220L,
        baseRewardXp = 48,
        baseDurationSec = 20,
        minSuspects = 0,
        maxSuspects = 1,
        baseEscapeRisk = 20,
        needsFastVehicle = true,
        escapeWindowSeconds = 100,
        reportTemplates = listOf(
            "برخورد دو خودرو در تقاطع اصلی رخ داده و راننده مقصر قصد ترک صحنه را دارد.",
            "تصادف زنجیره‌ای سبک در بلوار گزارش شده؛ مأمور راهور سریع اعزام کنید."
        )
    ),
    MISSING_PERSON(
        titleFa = "گم شدن افراد",
        preferredSpecialty = OfficerSpecialty.DETECTIVE,
        minLevel = 3,
        baseDifficulty = 2,
        requiredOfficers = 2,
        baseRequiredPower = 78,
        baseRewardMoney = 330L,
        baseRewardXp = 66,
        baseDurationSec = 24,
        minSuspects = 0,
        maxSuspects = 1,
        baseEscapeRisk = 18,
        needsFastVehicle = false,
        escapeWindowSeconds = 120,
        reportTemplates = listOf(
            "خانواده‌ای گزارش مفقود شدن فرزندشان در محدوده پارک را ثبت کرده‌اند.",
            "یک سالمند در اطراف ایستگاه مرکزی مسیر خود را گم کرده و نیاز به جستجوی سریع است."
        )
    ),
    STREET_FIGHT(
        titleFa = "درگیری خیابانی",
        preferredSpecialty = OfficerSpecialty.PATROL,
        minLevel = 4,
        baseDifficulty = 3,
        requiredOfficers = 2,
        baseRequiredPower = 104,
        baseRewardMoney = 450L,
        baseRewardXp = 86,
        baseDurationSec = 26,
        minSuspects = 2,
        maxSuspects = 4,
        baseEscapeRisk = 30,
        needsFastVehicle = false,
        escapeWindowSeconds = 85,
        reportTemplates = listOf(
            "نزاع میان چند نفر در خیابان اصلی گزارش شده؛ قبل از پراکنده شدن عاملان اقدام کنید.",
            "گزارش درگیری خیابانی در حوالی ایستگاه اتوبوس نیاز به محاصره سریع دارد."
        )
    ),
    CAR_THEFT(
        titleFa = "سرقت خودرو",
        preferredSpecialty = OfficerSpecialty.TRAFFIC,
        minLevel = 6,
        baseDifficulty = 3,
        requiredOfficers = 2,
        baseRequiredPower = 138,
        baseRewardMoney = 620L,
        baseRewardXp = 115,
        baseDurationSec = 30,
        minSuspects = 1,
        maxSuspects = 3,
        baseEscapeRisk = 42,
        needsFastVehicle = true,
        escapeWindowSeconds = 72,
        reportTemplates = listOf(
            "خودروی سرقتی با سرعت بالا به سمت خروجی شهر می‌رود! بدون خودروی سریع فرار می‌کند.",
            "سارق حرفه‌ای خودرو از پارکینگ عمومی گریخته؛ تیم تعقیب و راهور اعزام کنید."
        )
    ),
    CRIMINAL_PURSUIT(
        titleFa = "تعقیب مجرم",
        preferredSpecialty = OfficerSpecialty.TACTICAL,
        minLevel = 8,
        baseDifficulty = 4,
        requiredOfficers = 2,
        baseRequiredPower = 178,
        baseRewardMoney = 840L,
        baseRewardXp = 152,
        baseDurationSec = 34,
        minSuspects = 1,
        maxSuspects = 3,
        baseEscapeRisk = 48,
        needsFastVehicle = true,
        escapeWindowSeconds = 65,
        reportTemplates = listOf(
            "مظنون سابقه‌دار در بزرگراه شرقی مشاهده شده و در حال تغییر مسیر برای فرار است!",
            "واحدهای گشت درخواست پشتیبانی سریع برای بستن مسیر فرار مجرم دارند."
        )
    ),
    HOSTAGE_RESCUE(
        titleFa = "نجات گروگان",
        preferredSpecialty = OfficerSpecialty.NEGOTIATOR,
        minLevel = 11,
        baseDifficulty = 4,
        requiredOfficers = 3,
        baseRequiredPower = 235,
        baseRewardMoney = 1250L,
        baseRewardXp = 220,
        baseDurationSec = 40,
        minSuspects = 2,
        maxSuspects = 4,
        baseEscapeRisk = 36,
        needsFastVehicle = false,
        escapeWindowSeconds = 85,
        reportTemplates = listOf(
            "موقعیت حساس در ساختمان تجاری رخ داده؛ با مذاکره آرام شهروندان را آزاد و راه‌های فرار را ببندید.",
            "نیاز به تیم مذاکره‌کننده حرفه‌ای برای پایان ایمن بحران و جلوگیری از متواری شدن متهمان است."
        )
    ),
    JEWELRY_HEIST(
        titleFa = "سرقت از طلافروشی",
        preferredSpecialty = OfficerSpecialty.DETECTIVE,
        minLevel = 14,
        baseDifficulty = 5,
        requiredOfficers = 3,
        baseRequiredPower = 305,
        baseRewardMoney = 1780L,
        baseRewardXp = 295,
        baseDurationSec = 45,
        minSuspects = 3,
        maxSuspects = 5,
        baseEscapeRisk = 52,
        needsFastVehicle = true,
        escapeWindowSeconds = 68,
        reportTemplates = listOf(
            "آژیر بازار زرگران فعال شده! سارقان برنامه فرار دقیق دارند؛ تیم قوی و خودروی سریع بفرستید.",
            "سرقت سازمان‌یافته از گالری مرکزی گزارش شده؛ کوچک‌ترین تأخیر باعث فرار باند می‌شود."
        )
    ),
    HIGHWAY_CRISIS(
        titleFa = "مهار بحران بزرگراه",
        preferredSpecialty = OfficerSpecialty.TRAFFIC,
        minLevel = 16,
        baseDifficulty = 5,
        requiredOfficers = 3,
        baseRequiredPower = 360,
        baseRewardMoney = 2280L,
        baseRewardXp = 365,
        baseDurationSec = 48,
        minSuspects = 1,
        maxSuspects = 3,
        baseEscapeRisk = 40,
        needsFastVehicle = true,
        escapeWindowSeconds = 78,
        reportTemplates = listOf(
            "انسداد گسترده و فرار متخلفان در بزرگراه کمربندی؛ مسیر را ایمن و متهمان را متوقف کنید.",
            "عملیات ویژه راهنمایی و مهار متخلفان سریع در محور اصلی شهر آغاز شده است."
        )
    ),
    CITYWIDE_OPERATION(
        titleFa = "عملیات امنیت شهر",
        preferredSpecialty = OfficerSpecialty.TACTICAL,
        minLevel = 18,
        baseDifficulty = 5,
        requiredOfficers = 4,
        baseRequiredPower = 445,
        baseRewardMoney = 3100L,
        baseRewardXp = 480,
        baseDurationSec = 55,
        minSuspects = 4,
        maxSuspects = 7,
        baseEscapeRisk = 54,
        needsFastVehicle = true,
        escapeWindowSeconds = 75,
        reportTemplates = listOf(
            "عملیات هماهنگ برای دستگیری سرکرده‌های فراری و پاکسازی کامل مناطق کلیدی شهر.",
            "مأموریت کلان فرماندهی؛ تمام مسیرهای خروجی باید همزمان محاصره شوند تا کسی نگریزد."
        )
    );

    companion object {
        fun fromName(name: String): CrimeType =
            entries.firstOrNull { it.name == name } ?: SHOP_THEFT
    }
}

enum class MissionStatus {
    AVAILABLE,
    IN_PROGRESS,
    COMPLETED_SUCCESS,
    COMPLETED_FAIL,
    ESCAPED;

    companion object {
        fun fromName(name: String): MissionStatus =
            entries.firstOrNull { it.name == name } ?: AVAILABLE
    }
}

enum class TacticalAlertOption(
    val titleFa: String,
    val detailFa: String,
    val costCoins: Long,
    val successBoost: Int,
    val escapeReduction: Int,
    val extraSeconds: Int
) {
    BLOCK_EXITS(
        titleFa = "محاصره خروجی‌ها",
        detailFa = "رایگان • +۱۰٪ موفقیت • +۴ ثانیه",
        costCoins = 0L,
        successBoost = 10,
        escapeReduction = 14,
        extraSeconds = 4
    ),
    HOT_PURSUIT(
        titleFa = "تعقیب پرسرعت",
        detailFa = "با خودرو • +۱۶٪ موفقیت • بدون تأخیر",
        costCoins = 0L,
        successBoost = 16,
        escapeReduction = 20,
        extraSeconds = 0
    ),
    RADAR_BACKUP(
        titleFa = "پشتیبانی رادار",
        detailFa = "۴۵ سکه • +۲۲٪ موفقیت • مهار فرار",
        costCoins = 45L,
        successBoost = 22,
        escapeReduction = 28,
        extraSeconds = 0
    )
}

enum class EquipmentType(
    val titleFa: String,
    val effectDescFa: String,
    val baseCost: Long,
    val maxLevel: Int = 10
) {
    RADIO(
        titleFa = "بی‌سیم و ارتباطات",
        effectDescFa = "کاهش زمان انجام تمام مأموریت‌ها به میزان ۴٪ در هر سطح",
        baseCost = 280L
    ),
    RADAR(
        titleFa = "دوربین و رادار هوشمند",
        effectDescFa = "افزایش موفقیت (+۳٪) و کاهش شدید احتمال فرار مجرم (-۴٪) در هر سطح",
        baseCost = 340L
    ),
    SAFETY_GEAR(
        titleFa = "تجهیزات ایمنی و امداد",
        effectDescFa = "افزایش قدرت عملیاتی تیم در هر مأموریت (+۱۰ قدرت در هر سطح)",
        baseCost = 320L
    ),
    COMMAND_CENTER(
        titleFa = "سامانه هوشمند جرم‌یابی",
        effectDescFa = "افزایش پاداش نقدی مأموریت‌ها به میزان ۱۲٪ در هر سطح",
        baseCost = 420L
    )
}

enum class InmateStatus {
    IN_CUSTODY,
    BREAKOUT_ALERT,
    TRANSFERRED,
    ESCAPED_PRISON;

    companion object {
        fun fromName(name: String): InmateStatus =
            entries.firstOrNull { it.name == name } ?: IN_CUSTODY
    }
}

enum class PrisonUpgradeType(
    val titleFa: String,
    val effectDescFa: String,
    val baseCost: Long,
    val maxLevel: Int = 10
) {
    CELL_BLOCKS(
        titleFa = "بلوک سلول‌های بازداشتگاه",
        effectDescFa = "افزایش ظرفیت نگهداری زندانیان به میزان +۳ سلول در هر سطح",
        baseCost = 340L
    ),
    PRISON_SECURITY(
        titleFa = "امنیت و دوربین‌های بند",
        effectDescFa = "افزایش ضریب ایمنی زندان و کاهش شدید احتمال فرار از بند در هر سطح",
        baseCost = 390L
    ),
    INTERROGATION_WING(
        titleFa = "واحد بازجویی و پرونده‌سازی",
        effectDescFa = "افزایش پاداش تحویل زندانی (+۱۵٪) و جایزه کشف سرنخ در هر سطح",
        baseCost = 430L
    )
}

enum class ChiefTrait(
    val titleFa: String,
    val badgeFa: String,
    val bonusDescFa: String
) {
    TACTICAL_LEADER(
        titleFa = "فرمانده عملیات ویژه",
        badgeFa = "+۸٪ قدرت • +۵٪ موفقیت",
        bonusDescFa = "افزایش ۸٪ قدرت عملیاتی تیم و ۵٪ شانس موفقیت در تمام مأموریت‌های شهر"
    ),
    DETECTIVE_MIND(
        titleFa = "مغز متفکر کارآگاهی",
        badgeFa = "-۱۰٪ فرار • +۲۵٪ بازجویی",
        bonusDescFa = "کاهش ۱۰٪ خطر فرار مجرمان و افزایش ۲۵٪ پاداش بازجویی زندانیان"
    ),
    PURSUIT_ACE(
        titleFa = "استاد تعقیب و گریز",
        badgeFa = "-۱۲٪ زمان • مهار فراری",
        bonusDescFa = "کاهش ۱۲٪ زمان انجام عملیات‌ها و برتری ویژه در تعقیب فراری‌های زندان"
    ),
    CITY_ECONOMIST(
        titleFa = "مدیر ارشد بودجه",
        badgeFa = "+۱۵٪ درآمد و وثیقه",
        bonusDescFa = "افزایش ۱۵٪ درآمد نقدی مأموریت‌ها و پاداش تحویل زندانیان به دادگاه"
    );

    companion object {
        fun fromName(name: String): ChiefTrait =
            entries.firstOrNull { it.name == name } ?: TACTICAL_LEADER
    }
}

enum class ChiefAvatarOption(
    val key: String,
    val titleFa: String
) {
    TACTICAL("tactical", "فرمانده ویژه"),
    PATROL("patrol", "افسر گشت"),
    FEMALE("female", "فرمانده بانو"),
    DETECTIVE("detective", "کارآگاه ارشد");

    companion object {
        fun fromKey(key: String): ChiefAvatarOption =
            entries.firstOrNull { it.key == key } ?: TACTICAL
    }
}
