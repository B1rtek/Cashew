#include <bits/stdc++.h>
#include <chrono>
#include <thread>
#include <curl/curl.h>

using namespace std;

string mainFilePath="main.html";
string pictureFilePath="picture.html";

void downloadPage(string url, string saveFile)
{
    CURL *curl;
    FILE *fp;
    CURLcode res;
    char *curlurl = const_cast<char *>(url.c_str());
    curl = curl_easy_init();
    if (curl)
    {
        fp = fopen(saveFile.c_str(),"wb");
        curl_easy_setopt(curl, CURLOPT_URL, curlurl);
        curl_easy_setopt(curl, CURLOPT_WRITEFUNCTION, NULL);
        curl_easy_setopt(curl, CURLOPT_WRITEDATA, fp);
        res = curl_easy_perform(curl);
        curl_easy_cleanup(curl);
        fclose(fp);
    }
}

const int MODE_INFO = 1;
const int MODE_WARNING = 2;
const int MODE_ERROR = 3;
const int MODE_SUCCESS = 4;

void log(string message, int mode)
{
    string betterMessage;
    if(mode==MODE_INFO)
    {
        betterMessage = "[Info] ";
    }
    if(mode==MODE_WARNING)
    {
        betterMessage = "[Warn] ";
    }
    if(mode==MODE_ERROR)
    {
        betterMessage = "[!Err] ";
    }
    if(mode==MODE_SUCCESS)
    {
        betterMessage = "[Succ] ";
    }
    betterMessage += message;
    cout<<betterMessage<<endl;
}

int main()
{
    string caseID="2";
    string mode="Skins(caseID, itemName, rarity,";
    vector<string> SQL;
    string doZamiany = "&#039;";
    string podmianka="'";
    string doZamiany2 = "&quot;";
    string podmianka2 = "''";
    string lastPicURL = "btg30276";
    int errorState = 0;
    bool gloves=false;
    for(;;)
    {
        cout<<"> ";
        string url;
        cin>>url;
        if(url=="exit")
        {
            fstream output;
            output.open("output.sql", ios::out);
            for(int i=0; i<SQL.size(); i++)
            {
                output<<SQL[i]<<endl;
            }
            output.close();
            exit(0);
        }
        if(url=="setcase")
        {
            cin>>caseID;
            log("Case number/Knife group has been set to "+caseID, MODE_INFO);
            continue;
        }
        if(url=="mode")
        {
            cin>>url;
            if(url=="case")
            {
                mode="Skins(caseID, itemName, rarity,";
                gloves=false;
            }
            if(url=="knife")
            {
                mode="Knives(knifeGroup, itemName,";
                gloves=false;
            }
            if(url=="glove")
            {
                mode="Knives(knifeGroup, itemName,";
                gloves=true;
            }
            if(url=="collection")
            {
                mode="Skins(collectionID, itemName, rarity,";
                gloves=false;
            }
            log("Code generation mode has been changed to "+url, MODE_INFO);
            continue;
        }
        log("Processing "+url, MODE_INFO);
        downloadPage(url, mainFilePath);
        fstream source;
        source.open(mainFilePath.c_str(), ios::in);
        string linia, itemName, rarity, flavorText = "emptyFlavorLOL";
        int quality[5] = {-1,-1,-1,-1,-1};
        string pictureURLs[5] = {"empty","empty","empty","empty","empty"};
        while(getline(source, linia))
        {
            if(!gloves)
            {
                int skinNameIndex = linia.find("<h2><a href=\"");
                if(skinNameIndex!=-1)
                {
                    int skinNameBeginning = linia.find("\">")+2;
                    int skinNameEnd = linia.find("</a>");
                    itemName+=linia.substr(skinNameBeginning, skinNameEnd-skinNameBeginning);
                    itemName+=" | ";
                    skinNameBeginning = linia.find("\">", skinNameEnd)+2;
                    skinNameEnd = linia.find("</a>", skinNameBeginning);
                    itemName+=linia.substr(skinNameBeginning, skinNameEnd-skinNameBeginning);
                }
            }
            else
            {
                int skinNameIndex = linia.find("<h2>");
                if(skinNameIndex!=-1)
                {
                    int skinNameBeginning = skinNameIndex+4;
                    int skinNameEnd = linia.find("</h2>");
                    itemName = linia.substr(skinNameBeginning, skinNameEnd-skinNameBeginning);
                }
            }

            string rarityIndex = "<a class=\"nounderline\" href=\"https://csgostash.com/skin-rarity/";
            int skinRarityIndex = linia.find(rarityIndex);
            if(skinRarityIndex!=-1)
            {
                int skinRarityEnd = linia.find("\"", rarityIndex.size());
                rarity = linia.substr(rarityIndex.size(), skinRarityEnd-rarityIndex.size());
                if(rarity=="Consumer+Grade") rarity = "Consumer";
                if(rarity=="Industrial+Grade") rarity = "Industrial";
            }
            string flavorTextHTML = "<p><strong>Flavor Text:</strong> <em><a href=\"https://csgostash.com/lore\">";
            int flavorTextIndex = linia.find(flavorTextHTML);
            if(flavorTextIndex!=-1)
            {
                int flavorTextEnd = linia.find("</a>", flavorTextHTML.size());
                flavorText = linia.substr(flavorTextHTML.size(), flavorTextEnd-flavorTextHTML.size());
            }
            if(linia=="Factory New")
            {
                quality[0]=0;
            }
            if(linia=="Minimal Wear")
            {
                quality[1]=0;
            }
            if(linia=="Field-Tested")
            {
                quality[2]=0;
            }
            if(linia=="Well-Worn")
            {
                quality[3]=0;
            }
            if(linia=="Battle-Scarred")
            {
                quality[4]=0;
            }
            for(int i=0; i<5; i++)
            {
                if(quality[i]!=-1)
                {
                    quality[i]++;
                }
            }
            for(int i=0; i<5; i++)
            {
                if(quality[i]==4)
                {
                    int picURLBegin = 9;
                    int picURLEnd = linia.find("\"", picURLBegin);
                    pictureURLs[i] = linia.substr(picURLBegin, picURLEnd-picURLBegin);
                    quality[i]=-1;
                }
            }
        }
        source.close();
        //cout<<itemName<<endl<<rarity<<endl<<flavorText<<endl;
        for(int i=0; i<5; i++)
        {
            if(pictureURLs[i]=="empty")
            {
                continue;
            }
            if(i==4)
            {
                std::this_thread::sleep_for(std::chrono::milliseconds(2000));
            }
            std::this_thread::sleep_for(std::chrono::milliseconds(10000));
            downloadPage(pictureURLs[i], pictureFilePath);
            fstream pic;
            pic.open(pictureFilePath.c_str(), ios::in);
            string inlinia;
            while(getline(pic, inlinia))
            {
                string picURLBeginString = "<link rel=\"image_src\" href=\"";
                if(inlinia.find(picURLBeginString)!=-1)
                {
                    int picURLBegin = inlinia.find("href=\"")+6;
                    int picURLEnd = inlinia.find("\"", picURLBegin);
                    //cout<<pictureURLs[i]<<endl;
                    pictureURLs[i] = inlinia.substr(picURLBegin, picURLEnd-picURLBegin);
                }
            }
            pic.close();
            if(i==0)
            {
                if(pictureURLs[i]==lastPicURL && lastPicURL!="empty")
                {
                    log("[FN] An error occurred while processing this url, this insert is most likely corrupted", MODE_ERROR);
                    pictureURLs[i]="repairWillBeAttempted(TM)";
                    errorState=1;
                }
            }
            if(i==2)
            {
                if(pictureURLs[i]==lastPicURL && lastPicURL!="empty")
                {
                    log("[FT] An error occurred while processing this url, this insert is most likely corrupted", MODE_ERROR);
                    pictureURLs[i]="repairWillBeAttempted(TM)";
                    errorState=1;
                }
            }
            if(i==4)
            {
                if(pictureURLs[i]==lastPicURL && lastPicURL!="empty")
                {
                    log("[BS] An error occurred while processing this url, this insert is most likely corrupted", MODE_ERROR);
                    errorState=2;
                }
            }
            if(pictureURLs[i]!="empty")
            {
                lastPicURL=pictureURLs[i];
            }
            //cout<<pictureURLs[i]<<endl;
        }
        //naprawa b��d�w
        if(pictureURLs[0].size()<140 && pictureURLs[0]!="empty")
        {
            log("[FN] Attempting to repair a broken entry...", MODE_WARNING);
            if(pictureURLs[1]!="empty") pictureURLs[0]=pictureURLs[1];
        }
        if(pictureURLs[1].size()<140 && pictureURLs[1]!="empty")
        {
            log("[MW] Attempting to repair a broken entry...", MODE_WARNING);
            if(pictureURLs[0]!="empty") pictureURLs[1]=pictureURLs[0];
        }
        if(pictureURLs[2].size()<140 && pictureURLs[2]!="empty")
        {
            log("[FT] Attempting to repair a broken entry...", MODE_WARNING);
            if(pictureURLs[3]!="empty") pictureURLs[2]=pictureURLs[3];
        }
        if(pictureURLs[3].size()<140 && pictureURLs[3]!="empty")
        {
            log("[WW] Attempting to repair a broken entry...", MODE_WARNING);
            if(pictureURLs[2]!="empty") pictureURLs[3]=pictureURLs[2];
        }
        if(pictureURLs[4].size()<140 && pictureURLs[4]!="empty")
        {
            log("[BS] An error occurred while processing this url, this insert is most likely corrupted", MODE_ERROR);
            errorState=1;
        }
        while(itemName.find(doZamiany)!=-1)
        {
            int doZamianyIndex = itemName.find(doZamiany);
            itemName = itemName.replace(doZamianyIndex, doZamiany.size(), podmianka);
            log("[&#039;] Fixing weird HTML syntax in itemName...", MODE_WARNING);
        }
        while(flavorText.find(doZamiany)!=-1)
        {
            int doZamianyIndex = flavorText.find(doZamiany);
            flavorText = flavorText.replace(doZamianyIndex, doZamiany.size(), podmianka);
            log("[&#039;] Fixing weird HTML syntax in flavorText...", MODE_WARNING);
        }
        while(itemName.find(doZamiany2)!=-1)
        {
            int doZamianyIndex = itemName.find(doZamiany2);
            itemName = itemName.replace(doZamianyIndex, doZamiany2.size(), podmianka2);
            log("[&quot;] Fixing weird HTML syntax in itemName...", MODE_WARNING);
        }
        while(flavorText.find(doZamiany2)!=-1)
        {
            int doZamianyIndex = flavorText.find(doZamiany2);
            flavorText = flavorText.replace(doZamianyIndex, doZamiany2.size(), podmianka2);
            log("[&quot;] Fixing weird HTML syntax in flavorText...", MODE_WARNING);
        }
        if(flavorText[0]=='>')
        {
            flavorText.erase(0,1);
            log("[>] Fixing bugged flavorText...", MODE_WARNING);
        }
        string insertStatement = "INSERT INTO "+mode+" flavorText, fn, mw, ft, ww, bs) VALUES(";
        insertStatement+=caseID;
        insertStatement+=",\"";
        insertStatement+=itemName;
        if(mode=="Skins(caseID, itemName, rarity," || mode=="Skins(collectionID, itemName, rarity,")
        {
            insertStatement+="\",\"";
            insertStatement+=rarity;
        }
        insertStatement+="\",\"";
        insertStatement+=flavorText;
        for(int i=0; i<5; i++)
        {
            insertStatement+="\",\"";
            insertStatement+=pictureURLs[i];
        }
        insertStatement+="\");";
        SQL.push_back(insertStatement);
        if(errorState==0)
        {
            log("Entry done", MODE_SUCCESS);
        }
        if(errorState==2)
        {
            log("Rate limit exceeded, entering 20 seconds timeout", MODE_WARNING);
            std::this_thread::sleep_for(std::chrono::milliseconds(20000));
        }
        errorState=0;
    }
    return 0;
}
