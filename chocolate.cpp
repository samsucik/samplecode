#include <cstdlib>
#include <iostream>
#include <cmath>
#include <vector>

using namespace std;

// determines if a row (may be already a sum of multiple rows) can be cut into
// a desired number of parts while keeping the minimum desired richness
int satisfiableOnRows(vector <int> rows, int desiredRichness, int noOfColCuts){
    int currentNoOfCuts = 0, partialRichness = 0;

    // iterate through the row and make a cut every time the current group of squares
    // gives at least the desired richness
    for (int i = 0; i < rows.size(); i++){
        partialRichness += rows[i];
        if (partialRichness >= desiredRichness && currentNoOfCuts < noOfColCuts){
            currentNoOfCuts++;
            partialRichness = 0;
        }
    }
    // does each of currentNoOfCuts parts have at least desiredRichness
    // and is the number of parts the desired number?
    return (partialRichness >= desiredRichness && currentNoOfCuts == noOfColCuts);
}

// tries cutting the chocolate for a given desired richness
bool satisfiable(vector <vector <int> > chocolate, int noOfRowCuts, int noOfColCuts, int currentRichness){
    int currentNoOfCuts = 0;
    vector <int> row;
    vector <int> currentRows; // stores one or more rows which will make a horizontal part

    // iterate through the chocolate rows
    for (int i = 0; i < chocolate.size(); i++){
        row = chocolate[i];
        // add the current row to the current horizontal part
        if (currentRows.empty()){
            for (int j = 0; j < row.size(); j++){
                currentRows.push_back(row[j]);    //naplnenie vectora prvym riadkom novej casti
            }
        } else {
            for (int j = 0; j < row.size(); j++){
                currentRows[j] += row[j];         //pridavanie dalsich riadkov do currentRichnessj casti
            }
        }
        // if the horizontal part can be cut vertically given a desired richness, cut the part horizontally
        // off the rest of the chocolate and flush the horizontal part records
        if (satisfiableOnRows(currentRows, currentRichness, noOfColCuts) && currentNoOfCuts < noOfRowCuts){
            currentNoOfCuts++;
            currentRows.clear();
        }
    }
    return (satisfiableOnRows(currentRows, currentRichness, noOfColCuts) && currentNoOfCuts == noOfRowCuts);
}

// looks for the optimal richness in a whole chocolate
int goAndCut(vector <vector <int> > chocolate, int averageRichness, int noOfRowCuts, int noOfColCuts){
    int upperPivot = averageRichness, lowerPivot = 0, currentRichness;

    // binary search, trying richness between 0 and the average richness
    while(upperPivot - lowerPivot > 1){
        currentRichness = (upperPivot + lowerPivot)/2;

        if (satisfiable(chocolate, noOfRowCuts, noOfColCuts, currentRichness) == true){
            lowerPivot = currentRichness;
        } else {
            upperPivot = currentRichness;
        }
    }

    // if even the remaining rows can be cut in a satisfiable way, return the upperRichness
    // otherwise return lowerRichness, which has to be satisfiable
    if (satisfiable(chocolate, noOfRowCuts, noOfColCuts, upperPivot)){
        return upperPivot;
    } else {
        return lowerPivot;
    }
}

int main()
{
    int i, j, richness, noOfColumns, noOfRows, noOfRowParts, noOfColParts,
        averageRichness, noOfRowCuts, noOfColCuts;
    double mean;
    vector <vector <int> > chocolate;
    vector <int> rowAverages;

    // get dimensions and the number of parts
    cout << "Give me noOfRows, noOfColumns, noOfRowParts, noOfColParts" << endl;
    cin >> noOfRows >> noOfColumns >> noOfColParts >> noOfRowParts;

    // get the richness map of the chocolate
    // calculate average richness of one square for each row
    cout << "Give me the richness map of the chocolate" << endl;
    for (i = 0; i < noOfRows; i++){
        mean = 0;
        chocolate.push_back(vector <int>());
        for (j = 0; j < noOfColumns; j++){
            cin >> richness;
            chocolate[i].push_back(richness);
            mean += richness;
        }
        mean = mean/(1.0*noOfRowParts);
        rowAverages.push_back(int(floor(mean)));
    }

    // number of cuts = number of parts - 1
    noOfRowCuts = noOfColParts - 1;
    noOfColCuts = noOfRowParts - 1;

    // calculate overall average richness of one square
    mean = 0;
    for (i = 0; i < noOfRows; i++){
        mean += rowAverages[i];
    }
    averageRichness = int(mean/(1.0*noOfColParts));

    // go and do the actual work
    cout << goAndCut(chocolate, averageRichness, noOfRowCuts, noOfColCuts) << endl;
}
