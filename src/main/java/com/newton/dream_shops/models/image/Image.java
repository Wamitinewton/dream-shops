package com.newton.dream_shops.models.image;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.newton.dream_shops.models.product.Product;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Image {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String fileName;
    private String fileType;

    @Column(length = 2048)
    private String imageUrl;

    @Column(length = 1024)
    private String storagePath;


    @ManyToOne
    @JoinColumn(name = "product_id")
    @JsonIgnoreProperties("images")
    private Product product;
}
