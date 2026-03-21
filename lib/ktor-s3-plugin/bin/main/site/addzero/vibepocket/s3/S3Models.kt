package site.addzero.vibepocket.s3

data class S3Config(
    val endpoint: String = "https://s3.cstcloud.cn",
    val region: String = "us-east-1",
    val bucket: String = "af466fd92b0146ccbfb40cf590c912a0",
    val accessKey: String = "",
    val secretKey: String = ""
)
