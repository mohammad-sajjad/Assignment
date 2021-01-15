package com.pixel.saloonapp.app


class AppContextGoneException: Exception("App context is null, try calling init function of the " +
        "implementing class")