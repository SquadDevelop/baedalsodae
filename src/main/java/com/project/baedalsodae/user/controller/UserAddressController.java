package com.project.baedalsodae.user.controller;

import com.project.baedalsodae.auth.security.UserDetailsImpl;
import com.project.baedalsodae.global.common.ApiResponse;
import com.project.baedalsodae.global.common.SuccessCode;
import com.project.baedalsodae.user.dto.request.CreateUserAddressRequest;
import com.project.baedalsodae.user.dto.request.UpdateUserAddressRequest;
import com.project.baedalsodae.user.dto.response.UserAddressResponse;
import com.project.baedalsodae.user.service.UserAddressService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user-addresses")
@RequiredArgsConstructor
public class UserAddressController {

    private final UserAddressService userAddressService;

    @PostMapping
    public ApiResponse<Void> createAddress(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody CreateUserAddressRequest request) {
        userAddressService.createAddress(userDetails.getUserId(), request);
        return ApiResponse.success(SuccessCode.USER_ADDRESS_CREATED, null);
    }

    @GetMapping
    public ApiResponse<List<UserAddressResponse>> getAddressList(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        List<UserAddressResponse> response = userAddressService.getAddressList(userDetails.getUserId());
        return ApiResponse.success(SuccessCode.USER_ADDRESS_FOUND, response);
    }

	@GetMapping("/main")
	public ApiResponse<UserAddressResponse> getMainAddress(@AuthenticationPrincipal UserDetailsImpl userDetails) {
		UserAddressResponse response = userAddressService.getMainAddress(userDetails.getUserId());
		return ApiResponse.success(SuccessCode.USER_ADDRESS_FOUND, response);
	}

    @PutMapping("/{addressId}")
    public ApiResponse<UserAddressResponse> updateAddress(
            @PathVariable("addressId") UUID addressId,
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody UpdateUserAddressRequest request) {
        UserAddressResponse response = userAddressService.updateAddress(userDetails.getUserId(), addressId, request);
        return ApiResponse.success(SuccessCode.USER_ADDRESS_UPDATED, response);
    }

	@PutMapping
	public ApiResponse<Void> updateAddressList(
			@AuthenticationPrincipal UserDetailsImpl userDetails, @Valid @RequestBody List<UpdateUserAddressRequest> requests) {
		userAddressService.updateAddressList(userDetails.getUserId(), requests);
		return ApiResponse.success(SuccessCode.USER_ADDRESS_BULK_UPDATED, null);
	}

    @DeleteMapping("/{addressId}")
    public ApiResponse<Void> deleteAddress(
            @PathVariable("addressId") UUID addressId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        userAddressService.deleteAddress(userDetails.getUserId(), addressId);
        return ApiResponse.success(SuccessCode.USER_ADDRESS_DELETED, null);
    }

    @PatchMapping("/{addressId}/main")
    public ApiResponse<Void> changeMainAddress(
            @PathVariable("addressId") UUID addressId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        userAddressService.setMainAddress(userDetails.getUserId(), addressId);
        return ApiResponse.success(SuccessCode.USER_MAIN_ADDRESS_CHANGED, null);
    }
}
