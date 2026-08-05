package com.sample.system.card.service.application.openapi;

public class CardSecretOpenApiExamples {
    private CardSecretOpenApiExamples() {
    }

    public static final String GENERATE_OPERATION_DESCRIPTION = """
            Generate PIN1, PIN2 (OTP), or CVV2. از منوی Examples در Swagger نوع موردنظر را انتخاب کنید؛ بدنهٔ JSON همان ورودی‌های لازم برای همان type را نشان می‌دهد.
            برای PIN و OTP: pan، type، channel (و در صورت تمایل sessionType).
            برای CVV: علاوه بر آن‌ها expTime (۴ رقم YYMM) و serviceCode (۳ رقم) اجباری است.
            """;

    public static final String GENERATE_REQUEST_BODY_DESCRIPTION = "یکی از نمونه‌ها را انتخاب کنید (PIN / OTP / CVV)";

    public static final String VALIDATE_OPERATION_DESCRIPTION = """
            Validate PIN/OTP/CVV2. از منوی Examples نوع را انتخاب کنید.
            """;

    public static final String VALIDATE_REQUEST_BODY_DESCRIPTION = "یکی از نمونه‌ها را انتخاب کنید (PIN / OTP / CVV)";

    public static final class Generate {
        public static final String NAME_PIN = "type: PIN";
        public static final String SUMMARY_PIN = "تولید PIN";
        public static final String BODY_PIN = """
                {
                  "pan": "6037996449000002",
                  "type": "PIN",
                  "channel": "API",
                  "sessionType": "CARD_ISSUANCE"
                }
                """;

        public static final String NAME_OTP = "type: OTP";
        public static final String SUMMARY_OTP = "تولید OTP (PIN2)";
        public static final String BODY_OTP = """
                {
                  "pan": "6037996449000002",
                  "type": "OTP",
                  "channel": "API",
                  "sessionType": "CARD_ISSUANCE"
                }
                """;

        public static final String NAME_CVV = "type: CVV";
        public static final String SUMMARY_CVV = "تولید CVV2";
        public static final String BODY_CVV = """
                {
                  "pan": "6037996449000002",
                  "type": "CVV",
                  "channel": "API",
                  "sessionType": "CARD_ISSUANCE",
                  "expTime": "2601",
                  "serviceCode": "201"
                }
                """;
    }

    public static final class Validate {
        public static final String NAME_PIN = "type: PIN";
        public static final String SUMMARY_PIN = "اعتبارسنجی PIN — value اجباری";
        public static final String BODY_PIN = """
                {
                  "pan": "6037996449000002",
                  "type": "PIN",
                  "value": "1234"
                }
                """;

        public static final String NAME_OTP = "type: OTP";
        public static final String SUMMARY_OTP = "اعتبارسنجی OTP — value اجباری";
        public static final String BODY_OTP = """
                {
                  "pan": "6037996449000002",
                  "type": "OTP",
                  "value": "654321"
                }
                """;

        public static final String NAME_CVV = "type: CVV";
        public static final String SUMMARY_CVV = "اعتبارسنجی CVV2 — expTime و serviceCode اجباری";
        public static final String BODY_CVV = """
                {
                  "pan": "6037996449000002",
                  "type": "CVV",
                  "value": "123",
                  "expTime": "2601",
                  "serviceCode": "201"
                }
                """;
    }
}
