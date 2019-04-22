# BlockEQ Android Wallet
<a href="https://play.google.com/store/apps/details?id=com.blockeq.stellarwallet">
  <img alt="Download on the Play Store" title="App Store" src="https://user-images.githubusercontent.com/2541326/47521746-d57d8300-d861-11e8-8652-0f1cabcaed04.png" width="140"/>
</a>

## Project Status
[![Build Status](https://app.bitrise.io/app/aabf4854264f7412/status.svg?token=CLkPGE9X6FwMOTTBh1KqBA)](https://app.bitrise.io/app/aabf4854264f7412)
[![Slack chat](https://img.shields.io/badge/chat-on_slack-004FB9.svg?&logo=slack)](https://blockeq.slack.com)
[![Twitter](https://img.shields.io/twitter/url/https/github.com/block-equity/stellar-ios-wallet.svg?style=social)](https://twitter.com/block_eq)

## Table of Contents
- [Introduction](#introduction)
- [Features](#features)
- [Feedback](#feedback)
- [Contributing](./CONTRIBUTING.md)
- [Development](#development)
  - [Getting Started](#getting-started)
  - [Building the Project](#building-the-project)

## Introduction
BlockEQ is a private Stellar wallet that allows you to easily create or access an account with a recovery phrase. With BlockEQ you can send, receive, and trade XLM and Stellar tokens on the SDEX, Stellar’s decentralized exchange.

<p align="center">
  <img src ="https://blockeq.com/01d2b4822d66a99ac60aebf2f046b459.png" width=350>
</p>

## Features

A few of the main things the wallet supports:

* Add & remove Stellar Tokens
* Join an inflation pools
* QR code address display and scanning
* Use memo field for sending to exchanges
* Create and recover a new wallet with a 12 or 24 word mnemonic phrase
* Change PIN Settings (Toggle on/off for opening app or sending)

  
A few things coming soon:
* SDEX Trading
* Exchange address recognition (Memo Required)

## Feedback

Feel free to send us feedback on [Twitter](https://twitter.com/block_eq) or [file an issue](https://github.com/block-equity/stellar-android-wallet/issues/new). 

Feature requests are always welcome. If you wish to contribute, please take a quick look at the [guidelines](./CONTRIBUTING.md)!

If you just want to hang out and chat about BlockEQ, please feel free to join our [Slack Channel](https://blockeq.slack.com)!

## Development
Please take a look at the [contributing guidelines](./CONTRIBUTING.md) for a detailed process on how to build your application as well as troubleshooting information.

### Getting Started
* Install [Android Studio](https://developer.android.com/sdk/index.html)

### Building the Project

OS X, Windows & Linux:

```sh
git clone https://github.com/Block-Equity/stellar-android-wallet.git
```

1. `cd` into the project repo
2. Import the project. Open Android Studio, click `Open an existing Android
   Studio project` and select the project. Gradle will build the project.
3. Run the app. Click `Run > Run 'app'`. After the project builds you'll be
   prompted to build or launch an emulator.


If the above steps completed successfully, you're ready to begin developing! Otherwise, check out he troubleshooting section below.

### Troubleshooting

#### Build Errors
These are difficult to predict ahead of time, but general build error fixes include:
* Peforming a clean build
* `File > Invalidate Caches/ Restart`

If you still are having issues, an upstream dependency may have caused build errors, or there might be something specific to your environment. Feel free to open an issue if you find yourself in this situation.
