#include <bits/stdc++.h>

using namespace std;

int main()
{
	string linia, doZamiany, podmianka;
	getline(cin, linia);
//	cin>>doZamiany>>podmianka;
//	while(linia.find(doZamiany)!=-1)
//	{
//		int doZamianyIndex = linia.find(doZamiany);
//		linia = linia.replace(doZamianyIndex, doZamiany.size(), podmianka);
//		cout<<linia<<endl;
//	}
	for(int i=0; i<linia.size(); i++)
	{
		cout<<int(linia[i])<<endl;
	}
	return 0;
}
