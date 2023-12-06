package dev.mikita.userservice.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class Analyst extends User {
    private String name;
    private String description;
}