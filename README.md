# Easy Printer

## Przeznaczenie programu
Program Easy Printer powstał w celu usprawnienia procesu przygotowania dokumentacji konstrukcyjnej do przekazania do produkcji. Proces ten wiąże się z drukowaniem dużej ilości dokumentów PDF w różnych rozmiarach papieru, począwszy od rozmiaru A4, aż do powiększonych rozmiarów A0. Różne rozmiary papieru często wymagają wydruku na różnych urządzeniach, stąd konieczność rozdzielenia dokumentów pod względem wielkości formatu. Easy Printer ma za zadanie zautomatyzować tą czynność i wygenerować zbiorcze pakiety rysunków przygotowane do druku. Ponadto weryfikuje obecność odpowiednich dokumentów we wskazanym katalogu i tworzy strony tytułowe na podstawie wprowadzonych informacji o projekcie.

## Opis działania programu
1. Użytkownik wskazuje ścieżkę z dokumentami do wydrukowania (przycisk „Open directory”).
2. Program przeszukuje wskazany katalog (domyślnie max. 3 poziomy w głąb względem katalogu wyjściowego – możliwe do zmiany w pliku ustawień), w lewej dolnej części okna tworzy listę wszystkich znalezionych dokumentów PDF.
3. Listę główną program dzieli w tle na podlisty, w zależności od rozmiaru strony dokumentu.
4. Jeżeli wśród dokumentów znajduje się wielostronicowy PDF, którego strony mają co najmniej dwa różne rozmiary, program podzieli go na odrębne pliki i umieści je pod ścieżką „wskazana_ścieżka/_Split”.
5. Przycisk „Load ER” pozwala na wczytanie pliku Engineering Release (w formacie .xls lub .xlsx), na którego podstawie posortowane zostaną wczytane dokumenty. Nieposortowane elementy podświetlone są kolorem pomarańczowym, poprawnie posortowane elementy – kolorem zielonym, a elementy nieodnalezione w ER – kolorem czerwonym. Program wybierze z ER tylko te numery rysunków, które są przekazywane (są zaznaczone przez „x” albo liczbę dodatnią). Dodatkowo program pobierze z ER informację o BOM-liście – jeśli w ER w kolumnie BOM znajduje się „x”, to program poza rysunkiem będzie szukał też BOM-listy. Program pomija GL w tworzeniu listy odniesienia. Aby program zadziałał, ER musi spełniać następujące warunki:
    - Numery rysunków muszą znajdować się w kolumnie nazwanej „Draw. No.:”,
    - Kolumna zawierająca informację o BOM-listach musi nazywać się „BOM”
    - Aktualne przekazanie musi być zaznaczone kolorem innym niż biały (można zaznaczyć kilka przekazań – program stworzy wtedy jedną listę z uwzględnieniem ew. wycofania rysunków).
6. Przycisk „Load raw” pozwala na określenie kolejności  sortowania rysunków na podstawie pliku .xls lub .xlsx zawierającego tylko listę numerów rysunków, począwszy od komórki A1 w dół. Nie ma możliwości wczytania z takiej listy informacji o BOM-listach.  Podświetlenie kolorystyczne następuje wg tych samych reguł, co w przypadku wczytywania ER.
7. Przycisk „Merge” pozwala na automatyczne wygenerowanie połączonych plików pdf w jednej lokalizacji – pod ścieżką „wskazana_ścieżka/_Merged pdfs”.  Strony w pliku zachowają kolejność, wg której są ustawione w programie.
8. Przycisk „Clear” (menu „File”) pozwala na reset stanu programu i rozpoczęcie od początku (bez tego po wskazaniu innej ścieżki nowe pliki zostaną dopisane do bieżącej zawartości list).
9. W prawej części okna znajduje się konsola, w której wypisywana jest lista numerów, wg której następuje sortowanie (po wczytaniu ER lub pliku z surową listą). Wypisywane są tam również komunikaty o błędach (m.in. o plikach, które zostały odnalezione w ER, ale nie pod wskazaną ścieżką, oraz tych, które zostały znalezione w katalogu, ale nie ma ich w ER).

## Zrzuty ekranu

[Okno główne programu](pictures/okno_glowne.png)
[Wprowadzanie danych do okładek](https://github.com/mjdominiczak/easy-printer/pictures/dane_okladki.png)