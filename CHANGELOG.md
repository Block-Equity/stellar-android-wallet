# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Internal-2.0.2] 2018-12-14
## Fixed
- Re-fix: Market trade amount in second text field is not a dark enough font color
- Added generic error for all horizon exceptions sending funds on SendActivity.kt  

## [Internal-2.0.1] 2018-12-14
## Fixed
- GH-160 Make all buttons and toolbars look beautiful and consistent
- GH-154 Fixed all low priority issues in Android Bug Bash
- GH-152 Trading 100% should not do any math and grab 7 decimals
- GH-147 Update market price once user is back from limit
- GH-155 Creating asset does not refresh the list
- GH-151 Sending coins to yourself should be blocked
- GH-145 [UninitializedPropertyAccessException] crash switching fast form settings to trading

Other bug bash issues fixed in this version:
- Add asset is missing a back arrow on Action Bar
- 100% trade left a residual balance
- Market trade amount in second text field is not a dark enough font color
- Add asset: Font size too small
- Inflation destination: button colours are not the same as everywhere else in the app (Megha)
- Inflation destination: title can go into the action bar (right now it's in the main part of the screen)
- Send amount: Can not send amounts less than 4 decimal places
- Send amount: Cursor on memo should not be selected
- Diagnostic Email address incorrect support@com.blockeq
- Update market price once user is back from limit
- Buttons across the app are inconsistent in terms of font, size, format and color. This has to be consistent.
- Some titles are inconsistent (i.e. some action bar, some below, some have 'X', some have back arrow). Needs clean up.

## [Internal-2.0.0] - 2018-12-12
## Added
- GH-78 show exchange address in sending page
- Full support of Android App Bundle.
- Trade Feature: Trade, OrderBook and MyOffers
- GH-126 Trading asset selection logic in tradingTabFragment
- GH-142 Wallet List, setting trade row dot color to gray
- GH-127 Diagnostics for Android (v2.1)
- GH-123 create espresso tests for creation wallet flow, WalletManagement
- GH-114 About (Policies, ToS, Info)
- GH-109 Recovery with 15 or 18, 21 words
- GH-108 Add QR for mnemonic phrase
- Added a debug preferences to disable / enable leakCanary and the pin
- BIP30 mnemonic spell-check

## Changed
- GH-133 Remove backwards compatibility support for old wallets
- app id has changed from `blockeq.com.stellarwallet` to `com.blockeq.stellarwallet`
## Fixed
- GH-117 [java.lang.ClassCastException] getting the `getOldDecryptedPair()`
- GH-138 [IllegalStateException] Fragment already added: SearchableListDialog

## Security
- GH-103 [Security] check for pin when user changes `request pin when sending payments`

## [Internal-1.0.5] - 2018-11-21
## Changed
- GH-105 Email diagnostic tool
- New app icon with a beta ribbon on it
- "Beta" Added to the app title

*ATTENTION This will be the last update of the Beta app, as we will be freezing the code and developing the release app for a December release*

## [1.0.4.3] - 2018-11-19
## Fixed
- GH-98 [Empty passphrase bug]

## [1.0.4.2] - 2018-11-19
## Fixed
- GH-93 [StringIndexOutOfBoundsException] calling getOldDecryptedPair()
- GH-95 [Empty passphrase bug]

## Changed
- Changed the empty transactions string: Removed the words "Start trading!" Since it is not implemented yet.

## [1.0.4.1] - 2018-11-17
### Fixed
- Disabling and hiding the navigation item: `trading`, the feature was not read
(kotlin.UninitializedPropertyAccessException OrderBookTabFragment.updateTradingCurrencies)

## [1.0.4] - 2018-11-16
### Added
- Start using changelog
- GH-85 Add more checks for stellar recovery string and move the logic outside of the ui
- A fix for the crash in API19 around the security provider was applied.
- Adding support for spaces in the passphrase.
- GH-72 add a proper build CI and create a lint baseline file to ignore for now the 149 issues
- GH-58 Refactor input/strings checks to a class 
- Upgrading support libraries to 28
- Upgrading kotlin library from 1.2.50 to 1.2.71
- Adding ui test: WalletCreationTest and AccountUtilsTest and KeySToreWrapperTest
- Added Bitrise support for ui testing.
- TDD test for GH-52

### Changed
- Clearing the wallet was improved deleting any keystore’s alias.
- GH-66 Send funds message update

### Fixed
- GH-52 [IllegalArgumentException] creating wallet after entering to recovery secret key flow
- GH-56 The mnemonic is not fully visible in 480x800 hdpi devices
- GH-59 Support for Arabic RTL and non-Arabic LTR language
- Added a mechanism that checks clears the wallet when the shared preferences and the keystore are in a inconsistent state
- GH-79 [ArrayIndexOutOfBoundsException] loading the wallet screen
- GH-83 [BaseEncoding$DecodingException] with empty trying
- GH-63 Sending 100% of the available funds fails with a success message

### Security
- GH-70 Apply a secure random generator patch


## [1.0.3] - 2018-10-30
### Added
- Passphrase support (known limitation it does not support spaces)


## [1.0.2] - 2018-10-22
### Fixed
- Fixing the problem with non English locale and decimals (truncateDecimalPlaces())
- Fixing crash NumberFormatException and ClassCastException in the error response from horizon.
- 
### Added
- Improved the the error response for Horizon (uknown error)

## [1.0.1] - 2018-10-16
