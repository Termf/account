package com.binance.account.constants.enums;

public enum CountryImageUrlEnum {

    CN("CN", "中国","https://bin.bnbstatic.com/image/20200527/chinaflag.png"),
    KR("KR", "韩国","https://bin.bnbstatic.com/image/20200720/south-koreaflag.png"),
    TR("TR", "土耳其","https://bin.bnbstatic.com/image/20200720/turkeyflag.png"),
    VN("VN", "越南","https://bin.bnbstatic.com/image/20200720/vietnamflag.png"),
    RU("RU", "俄罗斯","https://bin.bnbstatic.com/image/20200720/russiaflag.png"),
    TW("TW", "台湾","https://bin.bnbstatic.com/image/20200818/taiwan@3x.png"),
    FR("FR", "法国","https://bin.bnbstatic.com/image/20200818/france@3x.png"),
    ES("ES", "西班牙","https://bin.bnbstatic.com/image/20200818/spain@3x.png"),
    DE("DE", "德国","https://bin.bnbstatic.com/image/20200818/germany@3x.png"),
    NL("NL", "荷兰","https://bin.bnbstatic.com/image/20200818/netherlands@3x.png"),
    NG("NG", "尼日利亚","https://bin.bnbstatic.com/image/20200818/nigeria@3x.png"),
    PL("PL", "波兰","https://bin.bnbstatic.com/image/20200818/republic-of-poland@3x.png"),
    ID("ID", "印尼","https://bin.bnbstatic.com/image/20200818/indonesia@3x.png"),
    UA("UA", "乌克兰","https://bin.bnbstatic.com/image/20200818/ukraine@3x.png"),
    PH("PH", "菲律宾","https://bin.bnbstatic.com/image/20200818/philippines@3x.png"),
    AU("AU", "澳大利亚","https://bin.bnbstatic.com/image/20200818/australia@3x.png"),
    HK("HK", "香港","https://bin.bnbstatic.com/image/20200818/hong-kong@3x.png"),
    IN("IN", "印度","https://bin.bnbstatic.com/image/20200818/india@3x.png"),
    GB("GB", "英国","https://bin.bnbstatic.com/image/20200818/united-kingdom@3x.png");




    private String code;
    private String cnDesc;
    private String countryImageUrl;

    CountryImageUrlEnum(String code, String cnDesc, String countryImageUrl) {
        this.code = code;
        this.cnDesc = cnDesc;
        this.countryImageUrl = countryImageUrl;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCnDesc() {
        return cnDesc;
    }

    public void setCnDesc(String cnDesc) {
        this.cnDesc = cnDesc;
    }

    public String getCountryImageUrl() {
        return countryImageUrl;
    }

    public void setCountryImageUrl(String countryImageUrl) {
        this.countryImageUrl = countryImageUrl;
    }


    public static CountryImageUrlEnum getCountryImageUrlEnummByCode(String code) {
        if (code == null){
            return null;
        }
        CountryImageUrlEnum[] values = CountryImageUrlEnum.values();
        for (CountryImageUrlEnum countryImageUrlEnum:values){
            if (countryImageUrlEnum.getCode().equals(code)){
                return countryImageUrlEnum;
            }
        }
        return null;
    }
}
