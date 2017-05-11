

package ucits;

public class Record {
    private String smnem;
    private double mcap;
    private double wgt;
    private double fixed_wgt;
    private double factor;
    private double fixing;
    private double allocated_wgt;
    private double final_wgt = 0;
    private char id;

    // Constructor
    public Record(String smnem2, double mcap2) {
        smnem = smnem2;
        mcap = mcap2;
        wgt = 0.0;
        fixed_wgt = 0.0;
        allocated_wgt = 0.0;
        final_wgt = 0.0;
        factor = 1;
        fixing = 0;
        id = 'I';
    }
    public Record() {}
    public String GetSmnem() {
        return smnem;
    }
    public double GetWgt() {
        return wgt;
    }
    public double GetMcap() {
        return mcap;
    }
    public char GetId() {
        return id;
    }
    public double GetFixing() {
        return fixing;
    }
    public double GetFactor() {
        return factor;
    }
    public double GetFixedWgt() {
        return fixed_wgt;
    }
    public double GetAllocWgt() {
        return allocated_wgt;
    }
    public double GetFinalWgt() {
        return final_wgt;
    }
    public void SetId(char sid) {
        id = sid;
    }
    public void SetWgt(double swgt) {
        wgt = swgt;
    }
    public void SetFixing(double sfixing) {
        fixing = sfixing;
    }
    public void SetFactor(double sfactor) {
        factor = sfactor;
    }

    public void SetFixedWgt(double snewwgt) {
        fixed_wgt = snewwgt;
    }
    public void SetAllocWgt(double salloc) {
        allocated_wgt = salloc;
    }
    public void SetFinalWgt(double sfinal) {
        final_wgt = sfinal;
    }
    public void SetSmnem(String ssmnem) {
        smnem = ssmnem;
    }
    public void SetMcap(double smcap) {
        mcap = smcap;
    }
