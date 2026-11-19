Explore KH - Cambodia Travel Guide
=================================

Explore KH is a Jetpack Compose tourism discovery prototype for Cambodia. It
now includes an interactive home experience, destination discovery, category
filters, attraction detail pages, saved places, profile statistics, and a
directions hand-off to the device's map application.

Implemented assignment features
--------------------------------
- Province and attraction catalog with heritage, nature, and beach categories
- Search across provinces and attractions
- Province detail and attraction detail navigation
- Bookmark/bucket-list state for saved attractions
- Profile summary and Material 3 bottom navigation
- Cambodia map overview and external GPS directions intent
- Local high-resolution image assets for offline display

The current data is local prototype data. A production version should connect
the models to Room, an authenticated REST API, WorkManager synchronization,
and a real map SDK as described in the assignment.

UI structure
------------
- `ui/components/CambodiaLogoComponent.kt`: reusable transparent Cambodia logo
- `ui/components/TopBarComponent.kt`: centered logo/title and notification bar
- `ui/components/SearchBarComponent.kt`: shared rounded search input
- `ui/components/ProvinceCarouselComponent.kt`: reusable province carousel
- `ui/components/explore/MapHeroComponent.kt`: map hero and map actions
- `ui/components/explore/AttractionCardComponents.kt`: attraction tiles and list cards
- `ui/components/explore/SectionTitleComponent.kt`: section headings and actions
- `ui/components/explore/DetailToolbarComponent.kt`: back/save toolbar
- `ui/components/explore/ExploreBottomBarComponent.kt`: primary navigation
- `ui/components/explore/ExploreColors.kt`: shared Explore design colors
- `ui/screen/ExploreScreens.kt`: home, province, discover, detail, saved, and
  profile screens
- `data/ExploreCatalog.kt`: local prototype catalog data
- `ui/screen/MainScreen.kt`: navigation/state coordinator only

The supplied map and Cambodia 25 logo PNGs were checked and retained as RGBA
assets with transparent pixels around the artwork, so they can be placed over
the navy UI without a rectangular background.

Image credits
-------------
- `angkor_wat_hd.jpg`: Diego Delso, Wikimedia Commons, CC BY-SA 3.0
  (downloaded from the 1280px version of “Angkor Wat, Camboya, 2013-08-15,
  DD 054.JPG”). See the original file page for required attribution.
- `koh_rong_hd.jpg` and `kampot_hd.jpg`: downloaded from Unsplash image URLs
  for prototype use. Confirm the current Unsplash license and attribution
  requirements before publishing the app.

Pre-requisites
--------------

You need to know:
- How to open, build, and run apps with Android Studio.
- How to create a basic app with one Activity and one layout
  that uses TextView, Button, and a basic layout such as LinearLayout.
- How to add a click handler to a Button.

Getting Started
---------------

1. Download and run the app.

License
-------

Copyright 2018 Google, Inc.

Licensed to the Apache Software Foundation (ASF) under one or more contributor
license agreements.  See the NOTICE file distributed with this work for
additional information regarding copyright ownership.  The ASF licenses this
file to you under the Apache License, Version 2.0 (the "License"); you may not
use this file except in compliance with the License.  You may obtain a copy of
the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
License for the specific language governing permissions and limitations under
the License.
