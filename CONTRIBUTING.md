# Contributing to BlockEQ

Thank you for taking the time to read this, and your interest in contributing to the project.

The following is a set of guidelines for contributing to [BlockEQ](https://blockeq.com) and its wallets, which are hosted in the [Block Equity organization](https://github.com/block-equity) on GitHub. These are mostly guidelines, not rules. Use your best judgment, and feel free to propose changes to this document in a pull request. There are many ways that you can contribute, beyond writing code. 

## Table of Contents
- [Contributing to BlockEQ](#contributing-to-blockeq)
  - [Table of Contents](#table-of-contents)
- [Asking and Answering Questions](#asking-and-answering-questions)
- [How Can I Contribute?](#how-can-i-contribute)
  - [Bounties](#bounties)
    - [**Bounty Rewards**](#bounty-rewards)
    - [**Working on a Bounty**](#working-on-a-bounty)
    - [**When it Becomes Complicated**](#when-it-becomes-complicated)
  - [Providing Feedback](#providing-feedback)
  - [Reporting Issues](#reporting-issues)
    - [**Look for an Existing Issue**](#look-for-an-existing-issue)
    - [**Writing Good Bug Reports and Feature Requests**](#writing-good-bug-reports-and-feature-requests)
    - [**Final Checklist**](#final-checklist)
- [Styleguides](#styleguides)
  - [Code](#code)
  - [Commit Messages and Git](#commit-messages-and-git)
  - [Documentation](#documentation)
  - [Testing](#testing)
- [Additional Information](#additional-information)
  - [Issue, Pull Request and Bounty Labels](#issue-pull-request-and-bounty-labels)
    - [Pull Requests](#pull-requests)
    - [Bounties](#bounties)
    - [General](#general)
- [Thank You! 🎉](#thank-you-%F0%9F%8E%89)

# Asking and Answering Questions
Have a question? Rather than opening an issue here, please ask away on [Stack Overflow](https://stackoverflow.com/questions/tagged/blockeq-android) using the tag '`blockeq-android`'.

There are people from [BlockEQ](https://blockeq.com) and the community there who will be eager to assist you. Your questions will serve as a better resource for others searching for help.

# How Can I Contribute?
## Bounties
Occasionally, [BlockEQ](https://blockeq.com) might post bounties for the community to help tackle bugs and features that we don't have the bandwidth for. Bounties are [BlockEQ](https://blockeq.com)'s way of giving something back to contributors for helping us accomplish our mission.

Bounties are issued at the discretion of each project's development team, and are valued _subjectively_ by individuals from [BlockEQ](https://blockeq.com) based on several factors:
* Importance
* Urgency
* Usefulness
* Quality

**NOTE:** The the reward amount between two bounties should never be compared - conditions and requirements under which they are issued change daily based on [BlockEQ](https://blockeq.com)'s roadmap, what we're working on, who issued it, as well as other factors.

Bounties have three simple states: `Offered`, `Accepted`, and `Closed`:  
* **Offered:** The bounty is available for the community to work on.  
* **Accepted:** Work has been submitted and has passed a review by the development team in order to claim the bounty.  
* **Closed:** The bounty is no longer available, either due to completion or removing it from being offered.  

### **Bounty Rewards**
Bounties are awarded in the form of [Block Points (PTS)](https://blockeq.com/block-points) and require that the recipient have a [Stellar](https://stellar.org) wallet capable of receiving `PTS`. Any Stellar wallet should be capable of supporting the token, but the BlockEQ Android & iOS wallets explictly support `PTS`.

### **Working on a Bounty**
An individual must submit their intent to officially work on a bounty. The first person to submit work that satisfies its requirements will be awarded the bounty.

If multiple efforts that satisfy bounty requirements are submitted, they will be reviewed in chronological order.

### **When it Becomes Complicated**
BlockEQ will review all submitted bounties and discuss openly on the corresponding issue for transparency to the community.

## Providing Feedback
Your comments and feedback are welcome, and members of the development team are available over a handful of different channels. You can reach out via the following ones:
* [GitHub Issues](https://github.com/block-equity/stellar-android-wallet/issues)
* [Slack](https://blockeq.slack.com/)
* [BlockEQ Subreddit](https://reddit.com/r/blockeq)
* [BlockEQ Email](mailto://hello@blockeq.com)

Whichever form you chose, please make sure to include as much contextual information as you can such as which wallet you're referring to, the version, etc.

## Reporting Issues
Have you identified a reproducible problem in the wallet? Do you have a feature request? We want to hear your input! Here's how you can effectively report your issue.

### **Look for an Existing Issue**
Before you create a new issue, please search in our [open issues](https://github.com/block-equity/stellar-android-wallet/issues) to see if the issue or feature request has already been filed.

Be sure to scan through the [most popular](https://github.com/block-equity/stellar-android-wallet/issues?q=is%3Aopen+is%3Aissue+label%3Afeature-request+sort%3Areactions-%2B1-desc) feature requests.

If you find your issue already exists, make relevant comments and add your [reaction](https://github.com/blog/2119-add-reactions-to-pull-requests-issues-and-comments). Use a reaction in place of a "+1" comment:

* 👍 - upvote  
* 👎 - downvote  

If you cannot find an existing issue that describes your bug or feature, create a new issue using the guidelines below.

### **Writing Good Bug Reports and Feature Requests**

File a single issue per problem and feature request. Please don't enumerate multiple bugs or feature requests in the same issue.

Also, don't add your issue as a comment to an existing issue unless it's for identical conditions, that's a great way for us to lose track of it. Many issues appear similar but have different causes.

The more information you can provide, the more likely someone will be successful reproducing the issue and finding a fix.

Please include the following with each issue:

* Version of the app

* A straightforward list of reproducible steps (1... 2... 3...) that cause the issue

* What you expected to see, versus what you actually saw

* Images, animations, or a link to a video showing the issue occurring

### **Final Checklist**

Please remember to do the following:

* [ ] Search the issue repository to ensure your report is a new issue

* [ ] Reproduce the issue

* [ ] Simplify your code around the issue to better isolate the problem

Don't lose hope if the developers can't reproduce the issue right away! They will try their best to do that, but it may require more time, patience, and information. Issues often arise unexpectedly, and there are other things that developers are working on. Depending on the severity/utility of the issue or feature request, we may not get around to it right away.

# Styleguides
## Code
For this project, we simply adhere to the code styling rules of `lint`, which is integrated into the project build process. Android lint will create warnings for minor violations, and errors for issues we don't permit. We aim to maintain a 0 warning, 0 error output for the project.

## Commit Messages and Git
Keeping a tidy commit history is helpful when moving back and forward to isolate when issues might have been introduced, so we prefer to have a single commit per feature or bug fix. Please, squash your commits up into a single one.

For bug fixes, create a short summary of the issue, prefixed with the text `[FIX]`, followed by a GitHub issue number, if applicable.

Example:

```
[FIX] Corrects project build failure (#13)
```

For new features, create a short summary of the issue, prefixed with the text `[FEATURE]`, followed by a GitHub issue number, if applicable.

```
[FEATURE] Adds support for NSUserActivity
```

## Documentation
Where possible and necessary, include class, function, and variable documentation for your work. 

## Testing
At a minimum, please provide unit tests along with your contributions. This helps us maintain a high standard of quality for the project.

If you are inclined to go as far as adding UI tests or snapshot tests for your features, that is incredibly helpful, and highly appreciated.

# Additional Information
## Issue, Pull Request and Bounty Labels
We have three different categories of labels we use for bounties, issues, and pull requests.

### Pull Requests
|Label|Description|Search|
|---:|---|:---:|
|pr-accepted|The pull request has been accepted|[🔍](https://github.com/Block-Equity/stellar-android-wallet/pulls?utf8=✓&q=is%3Apr+label%3Apr-accepted+)|
|pr-hold|The pull request should not be looked at or merged until later|[🔍](https://github.com/Block-Equity/stellar-android-wallet/pulls?utf8=✓&q=is%3Apr+label%3Apr-hold+)|
|pr-in-review|The pull request is currently undergoing review|[🔍](https://github.com/Block-Equity/stellar-android-wallet/pulls?utf8=✓&q=is%3Apr+label%3Apr-in-review+)|
|pr-review-required|The pull request needs attention|[🔍](https://github.com/Block-Equity/stellar-android-wallet/pulls?utf8=✓&q=is%3Apr+label%3Apr-review-required+)|

### Bounties
|Label|Description|Search|
|---:|---|:---:|
|bounty-accepted|The active bounty offer was accepted for this issue|[🔍](https://github.com/Block-Equity/stellar-android-wallet/labels/bounty-accepted)|
|bounty-closed|The bounty offer has been closed for this issue|[🔍](https://github.com/Block-Equity/stellar-android-wallet/labels/bounty-closed)|
|bounty-offer|An active bounty is available for this issue|[🔍](https://github.com/Block-Equity/stellar-android-wallet/labels/bounty-offer)|

### General
|Label|Description|Search|
|---:|---|:---:|
|blocked|This is being held up by something else|[🔍](https://github.com/Block-Equity/stellar-android-wallet/labels/blocked)|
|bug|Something isn't working|[🔍](https://github.com/Block-Equity/stellar-android-wallet/labels/bug)|
|duplicate|This issue or pull request already exists|[🔍](https://github.com/Block-Equity/stellar-android-wallet/labels/duplicate)|
|feature-request|New feature or request|[🔍](https://github.com/Block-Equity/stellar-android-wallet/labels/feature-request)|
|beginner|Good for newcomers|[🔍](https://github.com/Block-Equity/stellar-android-wallet/labels/beginner)|
|help-wanted|Extra attention is needed|[🔍](https://github.com/Block-Equity/stellar-android-wallet/labels/help-wanted)|
|invalid|This doesn't seem right|[🔍](https://github.com/Block-Equity/stellar-android-wallet/labels/invalid)|
|question|Further information is requested|[🔍](https://github.com/Block-Equity/stellar-android-wallet/labels/question)|
|wontfix|This will not be worked on|[🔍](https://github.com/Block-Equity/stellar-android-wallet/labels/wontfix)|

# Thank You! 🎉
Your contributions to open source, large or small, make great projects like this possible. Thank you for taking the time to contribute. 🙌 🙌 🙌