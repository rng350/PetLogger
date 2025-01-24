# **PetLogger**

PetLogger is a native Android application for Android for monitoring your pets' health.
Your device needs to be at least Android 5.0 in order to run the app on your phone.

This project still remains a work-in-progress, although enough has been done for it to achieve its primary purpose.

## **Features**
### **Pets**
To search for if a certain other enitity type is associated with a certain pet, type in:

pet:petname

or if the pet in question has more than one word in its name:

pet:"pet name"

You may include multiple pets in your search.
### **Weight Logs**
### **Event Logs**
Event logs may be attached to multiple pets.
### **Notes**
### **Photos**
### **Tags**
You may attach tags to pets, pet weight logs, event logs, notes, and photos. The intended purpose of tags is to facilitate data searching and organization.

To search if an item has a certain tag, type in:

#oneword

or if the item in question has more than one wordL

#"two words"

You may include multiple tags in your search.
### **Search**
If you ensure a certain substring in your query appears sequentially in any potential search results, put it between quotations.

"Just like this"

## **Architecture**
PetLogger implements an MVVM architecture. This app also uses XML views, coroutines and flows, Material3, and Room.
