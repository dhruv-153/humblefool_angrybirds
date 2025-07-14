#AngryBirds
DEMO VIDEO LINK - https://drive.google.com/file/d/1Xb_YcPnL1Bdnot7EKTt5NK2xU7OezI3u/view?usp=drive_link

## **Setup Instructions**

### **1. Download the Project**
- Download the ZIP file containing the project files from the provided link or GitHub repository.
- Extract the contents of the ZIP file to a desired location on your system.

---

### **2. (Optional) Create a New LibGDX Project Using gdx-liftoff**
To ensure compatibility and avoid configuration issues, it is recommended to create a new LibGDX project and replace the necessary files.

**Steps to create a new project using gdx-liftoff:**
1. Download and run the gdx-liftoff tool
2. Configure the project as follows:
    - **Project Name**: Use a name for your new project.
    - **Main Class**: Set it to `com.AngryBirds.Main`.
    - **Modules**: Ensure that both `core` and `lwjgl3` modules are selected.
    - **Dependencies**: Include Box2D and other required dependencies.
3. Generate the project.

**Replace the following from the ZIP file into the new project:**
- Replace the **`assets`** folder in `android/assets` (or `desktop/assets` for non-Android projects) with the `assets` folder from the ZIP.
- Replace the **`core`** folder in the new project with the `core` folder from the ZIP.
- Replace the **`build.gradle`** files (both root and module-specific) with the ones provided in the ZIP file.

---

### **3. Build the Gradle Files**
1. Open the project in your preferred IDE (e.g., IntelliJ IDEA or Eclipse).
2. Sync the Gradle files:
    - In IntelliJ, click **File > Sync Project with Gradle Files**.
    - In Eclipse, right-click the project > **Gradle > Refresh Gradle Project**.

3. Build the build.gradle files
4. Ensure that all dependencies (e.g., Box2D, LibGDX) are resolved correctly.

---

### **4. Set Run Configurations**
To execute the project, configure the run settings in your IDE.

#### **Run Configuration Settings:**
1. **Set JRE Version**:
    - Use **Oracle JDK 22** (or another compatible version as per the project requirements).

2. **Classpath**:
    - Ensure the classpath is set to the `lwjgl3` module's main directory.

3. **Main Class**:
    - Set the main class to:       com.AngryBirds.lwjgl3.Lwjgl3Launcher


#### **Example Run Configuration in IntelliJ IDEA:**
- Go to **Run > Edit Configurations**.
- Click the **+** icon and select **Application**.
- Fill in the following:
    - **Main class**: `com.AngryBirds.lwjgl3.Lwjgl3Launcher`.
    - **Use classpath of module**: `<PROJECT_NAME>.lwjgl3.main`.
    - **JRE**: Oracle JDK 22 (or specified version).

---

### **5. Run the Project**
1. Run the project from your IDE using the configured run settings.
2. The game should launch, and the Box2D physics engine should handle game logic and collisions.

---

### **6. Test Cases**
1. The test cases are located in `core/src/test`.
2. Execute Tests one at a time

