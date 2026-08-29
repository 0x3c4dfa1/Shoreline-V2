package net.shoreline.client.api.common;

import java.text.DecimalFormat;

public interface Formatted
{
    DecimalFormat WHOLE = new DecimalFormat("0");
    DecimalFormat DECIMAL = new DecimalFormat("0.0");
    DecimalFormat DECIMAL_TRIMMED = new DecimalFormat("0.0#");
}