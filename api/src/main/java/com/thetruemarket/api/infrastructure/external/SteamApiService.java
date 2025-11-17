package com.thetruemarket.api.infrastructure.external;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.thetruemarket.api.domain.exception.SkinImageResolutionException;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * Service for interacting with Steam Economy API.
 * Fetches asset class information to resolve skin image URLs.
 * Part of the Infrastructure layer (Frameworks & Drivers).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SteamApiService {

  private static final String INSTANCE_ID = "0";
  private static final String CLASS_COUNT = "1";

  private final RestTemplate restTemplate;

  @Value("${steam.api.key}")
  private String apiKey;

  @Value("${steam.api.cs2-app-id}")
  private String appId;

  @Value("${steam.api.base-url}")
  private String baseUrl;

  @Value("${steam.api.image-base-url}")
  private String imageBaseUrl;

  /**
   * Fetches the icon URL for a given class ID from Steam API.
   * 
   * @param classId The Steam class ID for the item
   * @return The full image URL
   * @throws SkinImageResolutionException if the API call fails or returns invalid
   *                                      data
   */
  public String getImageUrlByClassId(String classId) {
    if (classId == null || classId.isBlank()) {
      throw new IllegalArgumentException("Class ID cannot be null or empty");
    }

    try {
      String url = String.format(
          "%s/ISteamEconomy/GetAssetClassInfo/v1/?key=%s&appid=%s&class_count=%s&classid0=%s&instanceid0=%s",
          baseUrl, apiKey, appId, CLASS_COUNT, classId, INSTANCE_ID);

      log.info("Calling Steam API for class ID: {} - URL: {}", classId, url.replace(apiKey, "***"));

      SteamApiResponse response = restTemplate.getForObject(url, SteamApiResponse.class);

      if (response == null || response.getResult() == null) {
        log.error("Steam API returned null response for class ID: {}", classId);
        throw new SkinImageResolutionException("Steam API returned null response for class ID: " + classId);
      }

      log.debug("Steam API response received for class ID: {}, success: {}", classId, response.getResult().getSuccess());

      // Check if the API call was successful
      if (response.getResult().getSuccess() == null || !response.getResult().getSuccess()) {
        log.error("Steam API returned unsuccessful response for class ID: {}", classId);
        throw new SkinImageResolutionException("Steam API returned unsuccessful response for class ID: " + classId);
      }

      // Get the asset info from the dynamic map
      Object assetInfoObj = response.getResult().getClassInfoMap().get(classId);
      if (assetInfoObj == null) {
        log.error("Steam API did not return data for class ID: {}. Available keys: {}", classId, response.getResult().getClassInfoMap().keySet());
        throw new SkinImageResolutionException("Steam API did not return data for class ID: " + classId);
      }

      log.debug("Asset info found for class ID: {}, converting to AssetClassInfo", classId);

      // Convert the object to AssetClassInfo using ObjectMapper
      com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
      AssetClassInfo assetInfo = mapper.convertValue(assetInfoObj, AssetClassInfo.class);

      if (assetInfo == null || assetInfo.getIconUrl() == null) {
        log.error("Steam API did not return icon_url for class ID: {}. AssetInfo: {}", classId, assetInfo);
        throw new SkinImageResolutionException("Steam API did not return icon_url for class ID: " + classId);
      }

      String fullImageUrl = imageBaseUrl + assetInfo.getIconUrl();
      log.info("Successfully resolved image URL for class ID {}: {}", classId, fullImageUrl);

      return fullImageUrl;

    } catch (SkinImageResolutionException e) {
      throw e;
    } catch (Exception e) {
      log.error("Error calling Steam API for class ID {}: {}", classId, e.getMessage(), e);
      throw new SkinImageResolutionException("Failed to fetch image from Steam API for class ID: " + classId, e);
    }
  }

  /**
   * DTO for Steam API response
   */
  @Data
  private static class SteamApiResponse {
    private SteamResult result;
  }

  /**
   * DTO for Steam API result object containing asset info and success flag
   */
  @Data
  private static class SteamResult {
    @JsonProperty("success")
    private Boolean success;

    // This will catch all other properties (the class IDs)
    @com.fasterxml.jackson.annotation.JsonAnySetter
    private Map<String, Object> classInfoMap = new java.util.HashMap<>();
  }

  /**
   * DTO for asset class information
   */
  @Data
  @com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
  private static class AssetClassInfo {
    @JsonProperty("icon_url")
    private String iconUrl;

    private String name;

    @JsonProperty("name_color")
    private String nameColor;

    @JsonProperty("type")
    private String type;
  }
}