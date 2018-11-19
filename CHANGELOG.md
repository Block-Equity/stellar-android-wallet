# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]
- Trade support.
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
