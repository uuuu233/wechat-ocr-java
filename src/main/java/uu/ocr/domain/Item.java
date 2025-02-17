package uu.ocr.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Item {
    private double left;
    private double top;
    private double right;
    private double bottom;
    private double rate;
    private String text;
}
