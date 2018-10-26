package ua.com.atcorp.mobilecashdesk.rest.dto;

import java.util.ArrayList;

public class ItemDto {

    public class AdditionalField {
        public String name;
        public String value;
    };

    public String id;
    public String name;
    public String code;
    public String barCode;
    public String description;
    public Double price;
    public String companyId;
    public CompanyDto company;
    public String categoryId;
    public CategoryDto category;
    public String image;
    public boolean available;
    public ArrayList<AdditionalField> additionalFields;

}
