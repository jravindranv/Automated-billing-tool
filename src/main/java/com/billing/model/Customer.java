package com.billing.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Name is required")
    private String name;

    private String phone;
    private String email;
    private String address;

    /** Customer's state. If different from the shop's state, IGST is applied. */
    private String state;

    /** Optional GSTIN for B2B customers. */
    private String gstin;
}
