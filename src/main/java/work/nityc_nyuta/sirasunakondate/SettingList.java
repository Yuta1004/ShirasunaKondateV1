package work.nityc_nyuta.sirasunakondate;


public class SettingList {
    long id;
    int Image_id;
    String Setting;
    String Value;

    public long getId() {return id;}
    public void setId(long id) {this.id = id;}

    public int getImage_id(){ return Image_id; }
    public void setImage_id(int Image_id){ this.Image_id = Image_id; }

    public String getSetting() {
        return Setting;
    }
    public void setSetting(String Setting) {
        this.Setting = Setting;
    }

    public String getValue() {
        return Value;
    }
    public void setValue(String Value) {
        this.Value = Value;
    }

}
