package com.pryv.functional;

import com.pryv.auth.AuthView;

/**
 * authentication view used for testing
 *
 * @author ik
 *
 */
public class FakeAuthView implements AuthView {

  private Boolean displayLoginViewExecuted = false;
  private Boolean displaySuccessExecuted = false;
  private Boolean displayFailureExecuted = false;

  @Override
  public void displayLoginVew(String loginURL) {
    displayLoginViewExecuted = true;
  }

  @Override
  public void onDisplaySuccess(String username, String token) {
    displaySuccessExecuted = true;
  }

  @Override
  public void onDisplayFailure() {
    displayFailureExecuted = true;
  }

  public Boolean getDisplayLoginViewExecuted() {
    return displayLoginViewExecuted;
  }

  public Boolean getDisplaySuccessExecuted() {
    return displaySuccessExecuted;
  }

  public Boolean getDisplayFailureExecuted() {
    return displayFailureExecuted;
  }

}
