# StripeRust-Pocket

This repository contains the code and dataset for the paper "StripeRust-Pocket: A Mobile-based Deep Learning Application for Efficient Disease Severity Assessment of Wheat Stripe Rust," accepted at Plant Phenomics. StripeRust-Pocket can accurately measure the severity of wheat stripe rust disease by taking photos or selecting photos from albums. It can also organize the result data and save the results in an Excel format and send them via email.

## Start

First of all, due to the presence of large files in the repository, please go to the official website of git lfs to download the tool.

https://git-lfs.com/

Then, clone the code
```shell script
git clone https://github.com/WeizhenLiuBioinform/StripeRust-Pocket.git
git lfs pull
```

## Application-Installation

The installation files for the complete APP are located in the application folder. Supports phones with Android version 9.0 or above.

### How to use the APP
Our APP consists of three pages, and the detailed content and related usage methods of each page are as follows：

1.The startup page
This page initializes the various modules and loads the StripeRustNet deep learning model. After clicking the "Start" button, users are guided to the main page.
![The startup page](https://github.com/WeizhenLiuBioinform/StripeRust-Pocket/blob/master/Application_source_code/app/src/main/assets/The_startup_page.jpg)

2.The main page
The main page consists of a central display area and three buttons: "Photo," "Severity," and a toggle button in the top left corner. By clicking the "Photo" button, a selection box will pop up, allowing users to choose to capture images using the built-in camera or select images directly from their phone gallery. The selected image is displayed in the central display area.Users can enter the result image's ID and date and save it on their phone. After clicking the save button, the segmentation result will also be displayed on the main page. By clicking the toggle button in the top right corner, users can switch to the results display page. 
![The main page](https://github.com/WeizhenLiuBioinform/StripeRust-Pocket/tree/master/Application_source_code/app/src/main/assets/The_main_page.jpg)

3.The result display page
The result display page presents the visual outcome of lesion segmentation, user-defined ID, and date, and the final disease severity assessment for the tested image. Additionally, users can modify or delete the ID and date for each result by clicking on the respective result. By clicking the white button in the top right corner again, users can navigate to the Excel page. This page initially displays all the tested image's disease severity results in the form of an Excel spreadsheet. Moreover, the generated spreadsheet can be directly sent via email by clicking the button in the top right corner, facilitating result analysis and sharing.
![The result display page](https://github.com/WeizhenLiuBioinform/StripeRust-Pocket/tree/master/Application_source_code/app/src/main/assets/The_result_display_page.jpg)


## Application-Code
### Prerequisites
* Android studio 2021.3.1

### Introduction
The application comprises five modules: the image acquisition module, image segmentation module, disease severity 
quantification module, result display module, and result export module.

1. MainActivity_bg.java: Its main function is to display the start page.
2. MainActivity.java: This file is the core of the entire app. It integrates functions such as loading deep learning 
models, calling pop-up windows, calling models for image segmentation, displaying running results, saving images, 
and calculating disease severity.
3. MainActivity_table.java and MainActivity_table_2.java: They respectively represent the presentation of results in 
the form of images and the presentation of results in the form of Excel spreadsheets. Among them, 
MainActivity_table_2 uses Smart Table technology, as detailed in https://github.com/huangyanbin/smartTable
4. The two deep learning models used in the APP are both in /app/src/main/assets.
5. All static files are located in/app/src/main/res/layout. The style settings file and string settings file are 
located in/app/src/main/res/values.
