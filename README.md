### Simple IDE for Android  

**Lightweight Android IDE**. Originally created as an IDE for web development, it was later redesigned to create Android apps as an experiment. After years of intermittent development, it reached a functional state in 2024. Unable to publish on Google Play due to lack of testers, I'm open-sourcing it here - hoping someone finds the code useful.  

---

### Key Features  
- **Only Java**: Kotlin *not* supported
  (but there's an unused compiler class that possibly can be easily implemented)
- **XML Layouts**: XML for UI design  
- **Custom Build System**:  
  - Faster than Gradle but less feature-rich
- **Library Management**:  
  - Download any library manually  
  - Pre-installed libraries in template project 
- **Code Formatting**: Java, XML, and JSON files  

---

### Project History  
- 🌐 **Started as an IDE for web development** years ago  
- 📱 **Rebuilt for creating Android apps** as a personal experiment  
- ⏳ **Development paused multiple times** due to life circumstances  
- 🚀 **Basic functionality completed** in 2024  
- ❌ **Failed Google Play publish** (insufficient testers)  
- 💡 **Now open-sourced** to avoid wasting the effort  

---

### Get app
1. Download latest APK from actions
2. Install on Android 12+ device
3. Open → Create New Project → Start coding

---

### Further Development?  
*Continuation of work depends on community interest.* This project **doesn't compete** with advanced IDEs like [AndroidIDE](https://github.com/AndroidIDEOfficial/AndroidIDE) since it lacks many useful features like multi-module projects (though fixable), kotlin support, visual xml, etc.

Potential TODO:

- [ ] TreeView rework (Luminarr-style)  
- [ ] Backport other fixes/features from [Luminarr](https://github.com/vxhjsd/luminarr)  
- [ ] Project Settings Manager  
- [ ] Libraries Manager  
- [ ] Resources Manager  
- [ ] Build system enhancements (based on [XedoxDev](https://github.com/XedoxDev) developments)  

---

### Why Use This?  
If you need to quickly test a few lines or create simple app, like a clicker, this might help. For complex projects, consider more feature-rich alternatives.  

> **Note**: This is not a polished product. Bugs exist. Contributions welcome!

---

### License

<img src="https://github.com/vxhjsd/Simple-IDE/blob/master/assets/license.png" alt="GPLv3 License" style="width: 225px; height: auto;" />

This project is licensed under the [**GPLv3**](https://github.com/vxhjsd/Simple-IDE/blob/master/LICENSE)
