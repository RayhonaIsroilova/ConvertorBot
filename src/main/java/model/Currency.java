package model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class Currency {
    private int id;
    private String CcyNmEN;
    private String CcyNmUZC;
    private String Diff;
    private String Rate;
    private String Ccy;
    private String CcyNmRU;
    private String CcyNmUZ;
    private String Code;
    private String Nominal;
    private String Date;
}
