package com.itimpulse.urlshortener.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CustomResponse<T> {
  @Schema(description = "Response message", example = "Short URL created successfully")
  private String message;

  @Schema(description = "HTTP status code", example = "201")
  private int statusCode;

  @Schema(description = "Response data payload")
  private T data;

  private CustomResponse(String message, int statusCode, T data) {
    this.message = message;
    this.statusCode = statusCode;
    this.data = data;
  }

  private CustomResponse(String message, int statusCode) {
    this.message = message;
    this.statusCode = statusCode;
  }

  public static <T> CustomResponse<T> successResponse(String message, int statusCode, T data) {
    return new CustomResponse<>(message, statusCode, data);
  }

  public static <T> CustomResponse<T> successResponse(String message, int statusCode) {
    return new CustomResponse<>(message, statusCode);
  }

  public static <T> CustomResponse<T> errorResponse(String message, int statusCode) {
    return new CustomResponse<>(message, statusCode);
  }
}
