package com.binance.account.util;

import java.io.IOException;
import java.math.BigDecimal;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class BigDecimalToStringSerializer extends JsonSerializer<BigDecimal> {
    @Override
    public void serialize(BigDecimal value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
        if (value != null) {
            jgen.writeString(value.stripTrailingZeros().toPlainString());
        } else {
            jgen.writeString("0");
        }
    }

}
