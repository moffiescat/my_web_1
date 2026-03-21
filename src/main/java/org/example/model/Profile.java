package org.example.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Profile {

    @NotBlank(message = "姓名不能为空")
    @JsonProperty("name")
    private String name;

    @Min(value = 0, message = "年龄不能小于0")
    @JsonProperty("age")
    private int age;

    @NotBlank(message = "职业不能为空")
    @JsonProperty("profession")
    private String profession;

    @Email(message = "邮箱格式不正确")
    @NotBlank(message = "邮箱不能为空")
    @JsonProperty("email")
    private String email;
}
