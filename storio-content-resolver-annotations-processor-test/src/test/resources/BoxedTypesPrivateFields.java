package com.pushtorefresh.storio.contentresolver.annotations;

@StorIOContentResolverType(uri = "content://uri")
public class BoxedTypesPrivateFields {

    @StorIOContentResolverColumn(name = "field1")
    private Boolean field1;

    @StorIOContentResolverColumn(name = "field2")
    private Short field2;

    @StorIOContentResolverColumn(name = "field3")
    private Integer field3;

    @StorIOContentResolverColumn(name = "field4", key = true)
    private Long field4;

    @StorIOContentResolverColumn(name = "field5")
    private Float field5;

    @StorIOContentResolverColumn(name = "field6")
    private Double field6;

    public Boolean getField1() {
        return field1;
    }

    public void setField1(Boolean field1) {
        this.field1 = field1;
    }

    public Short getField2() {
        return field2;
    }

    public void setField2(Short field2) {
        this.field2 = field2;
    }

    public Integer getField3() {
        return field3;
    }

    public void setField3(Integer field3) {
        this.field3 = field3;
    }

    public Long getField4() {
        return field4;
    }

    public void setField4(Long field4) {
        this.field4 = field4;
    }

    public Float getField5() {
        return field5;
    }

    public void setField5(Float field5) {
        this.field5 = field5;
    }

    public Double getField6() {
        return field6;
    }

    public void setField6(Double field6) {
        this.field6 = field6;
    }
}