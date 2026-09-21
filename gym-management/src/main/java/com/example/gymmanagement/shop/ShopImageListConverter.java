package com.example.gymmanagement.shop;
import jakarta.persistence.AttributeConverter; import jakarta.persistence.Converter; import com.fasterxml.jackson.databind.ObjectMapper; import java.util.*;
@Converter public class ShopImageListConverter implements AttributeConverter<List<String>,String> {
 private static final ObjectMapper JSON=new ObjectMapper();
 public String convertToDatabaseColumn(List<String> v){try{return JSON.writeValueAsString(v==null?List.of():v);}catch(Exception e){throw new IllegalArgumentException(e);}}
 public List<String> convertToEntityAttribute(String v){try{return v==null||v.isBlank()?new ArrayList<>():JSON.readValue(v,JSON.getTypeFactory().constructCollectionType(ArrayList.class,String.class));}catch(Exception e){throw new IllegalArgumentException(e);}}
 public static List<String> validate(List<String> urls){if(urls==null)return new ArrayList<>();if(urls.size()>8)throw new IllegalArgumentException("Tối đa 8 ảnh");for(String url:urls)if(url==null||url.length()>2000||!(url.startsWith("https://")||url.startsWith("/api/files/")))throw new IllegalArgumentException("Ảnh phải là URL HTTPS hoặc ảnh đã tải lên");return new ArrayList<>(urls);}
}
