# **PetLogger**
PetLogger is a native Android application for monitoring your pets' health.

Although this remains a work-in-progress, enough has been done for it to achieve its primary purpose.

## **Requirements**
Note: Your device needs to have at least Android 5.0 installed in order to run the app.

## **Features**
The app features multiple entity types with very inter-connected relationships.
### **Pets**
To look up if a certain other entity type is associated with a certain given pet, type in:

pet:petname

You may include multiple pets in your search.
### **Weight Logs**
Weight logs are attached to only a single pet.
### **Event Logs**
Event logs may be attached to multiple pets.
### **Notes**
### **Photos**
### **Tags**
You may attach tags to pets, pet weight logs, event logs, notes, and photos. The intended purpose of tags is to facilitate data searching and organization.

To search if an item has a certain tag, type in:

#tagname

You may include multiple tags in your search.
### **Search**
If you wish to ensure a certain substring in your query appears sequentially in any potential search results, put it between quotations.

"Just like this"

This also applies to pet, tag queries, and everything else.

pet:"John Smith"
#"tag with multiple words"

## **Architecture**
PetLogger implements an MVVM architecture. This app also uses XML views, coroutines and flows, Material3, and Room.
