# agentske-tehnologije
Projekat baziran na specifikaciji datoj na kursu "Agentske tehnologije", Fakultet tehničkih nauka, Novi Sad, 2020.

Aplikaciju na host-u sam deploy-ovao na adresi preko koje host mašina komunicira sa virtualnom mašinom, a aplikaciju na virtualnoj sam deploy-ovao na adresi preko koje virtualna mašina komunicira sa host-om. Potrebno je samo promeniti hostname wildfly servera na odgovarajuće adrese da bi funkcionisala komunikacija između servera. Takođe na mašini na kojoj se nalaze slave čvorovi potrebno je uneti adresu master čvora u connection.properties fajl koji se nalazi u beans packege-u u .jar projektu u formatu adresa:port da bi slave čvorovi znali ko je master.  
