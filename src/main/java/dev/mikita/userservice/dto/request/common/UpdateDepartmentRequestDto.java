package dev.mikita.userservice.dto.request.common;

import dev.mikita.userservice.annotation.NullCheck;
import lombok.Data;

import java.util.List;

@Data
public class UpdateDepartmentRequestDto {

    @NullCheck(message = "Specify a name")
    String name;
    @NullCheck(message = "Specify the description")
    String description;
    @NullCheck(message = "Specify the address")
    String address;
    @NullCheck(message = "Specify the phone number")
    String phoneNumber;
    @NullCheck(message = "Specify the categories")
    List<Long> categories;
}
