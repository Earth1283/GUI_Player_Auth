### **GUI Player Authentication Plugin - Secure Your Server with PIN Authentication**  

🔒 **GUI Player Authentication** is a **Minecraft plugin** that enhances server security by requiring players to **set up and enter a secure PIN** before they can interact with the world. This plugin prevents unauthorized access, ensuring that only verified players can play.

---

## **🔹 Features**
✅ **PIN Registration & Login:** Players must register a **4-digit PIN** (or any other configured value) on their first login and use it for future authentications.  
✅ **Custom Chest GUI:** Players enter their PIN using a visually appealing **Chest GUI** with **Light Blue Dye items** representing digits.  
✅ **Auto-Kick System:** If a player **fails to authenticate within 30 seconds**, they will be **automatically kicked** from the server.  
✅ **PIN Verification:** If the entered PIN is incorrect, the player is **kicked immediately** to prevent brute-force attempts.  
✅ **Player Restrictions:** Until authentication is complete, players **cannot move, chat, execute commands, or interact** with the world.  
✅ **Blindness Effect:** Unauthenticated players receive a **permanent blindness effect** until they log in successfully.  
✅ **PIN Reset Command (`/resetpin`)**: Players can reset their PIN and register a new one at any time.  

---

## **🔹 How It Works**
1. **New Players:**  
   - Upon joining, if the player **has no PIN registered**, they are shown a **Register GUI** where they must **select a 4-digit PIN** (or however many digits the user inputs in `config.yml`.  
   - Once confirmed, the PIN is **stored securely in a SQLite database**, and the player is granted access to the server.  

2. **Returning Players:**  
   - If the player already has a PIN, they are shown the **Login GUI** and must enter their **correct PIN**.  
   - A correct PIN **removes all restrictions**, while an incorrect PIN results in an **immediate kick**.  

3. **Auto-Kick for Unauthenticated Players:**  
   - If a player **does not authenticate within 30 seconds**, they are **automatically kicked** from the server.  

4. **PIN Reset:**  
   - Players can use **`/resetpin`** to delete their PIN and start the registration process again.  

---

## **🔹 Commands & Permissions**
| Command        | Description | Permission |
|---------------|-------------|------------|
| `/resetpin`   | Resets the player's PIN and prompts for re-registration. | **All Players** |

---

## **🔹 Configuration (`config.yml`)**
```yaml
# The number of digits required for PIN authentication
pin_length: 4
```
- **Customizable PIN length** if needed.  

---

## **🔹 Why Use This Plugin?**
🔹 **Prevents Account Hijacking:** Ensures that only the real player can log into their account.  
🔹 **Brute-Force Protection:** Instant kicks for incorrect PIN attempts prevent unauthorized access.  
🔹 **User-Friendly GUI:** Players interact through an **intuitive Chest GUI** instead of typing commands.  
🔹 **Lightweight & Efficient:** Uses **SQLite storage** for easy setup and **low server impact**.  
🔹 **Perfect for Private & Public Servers:** Ideal for **SMPs, Minigame servers, and RP servers** that need an extra layer of security.  

---

## **🔹 Installation**
1. **Download the Plugin `.jar` File.**  
2. **Place it in the `/plugins` folder** of your Minecraft server.  
3. **Restart the server.**  
4. **Customize settings in `/plugins/GUIPlayerAuth/config.yml` (optional).**  
5. **Enjoy a more secure Minecraft experience!**  

---

### **💾 Compatibility**
✅ Supports **Minecraft 1.21+**  
✅ Works with **Paper, Spigot, and Bukkit** servers  

---

### **🔹 Future Updates & Features**
🔜 **MySQL Support** for multi-server authentication  
🔜 **Configurable Auto-Kick Time**  
🔜 **Admin Override Commands**  

---

🎉 **Secure your server today with GUI Player Authentication!** 🎉  
