import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Scanner;

public class Simplex {
    private static int findMaxValIndex(double[] z) {
        int maxInd = 0;
        for (int i = 0; i < z.length; i++)
            maxInd = z[i] > z[maxInd] ? i : maxInd;
        return maxInd;
    }

    private static int findMinPosValue_b_div_a_Index(double[][] simplexMatrix, int z_MaxValIndex) {
        ArrayList<Double> b_div_a_list = new ArrayList<>();

        // Finding minimum positive value of division
        for (int i = 0; i < simplexMatrix.length - 1; i++)
            b_div_a_list.add(simplexMatrix[i][simplexMatrix[i].length - 1] / simplexMatrix[i][z_MaxValIndex]);

        double maxVal = Collections.max(b_div_a_list);
        int minPosValueIndex = b_div_a_list.indexOf(maxVal);

        // Get the index of minimum positive value
        for (int i = 0; i < b_div_a_list.size(); i++) {
            if (b_div_a_list.get(i) < b_div_a_list.get(minPosValueIndex) && b_div_a_list.get(i) > 0) {
                b_div_a_list.set(i, b_div_a_list.get(i));
                minPosValueIndex = i;
            }
        }
        return minPosValueIndex;
    }

    private static int[] findOperableRowAndColumn(double[][] simplexMatrix) {
        int z_MaxValIndex = findMaxValIndex(simplexMatrix[simplexMatrix.length - 1]);
        int operableRowIndex = findMinPosValue_b_div_a_Index(simplexMatrix, z_MaxValIndex);
        return new int[]{operableRowIndex, z_MaxValIndex};
    }

    static long findGreatestDivisor(long a, long b) {
        if (b == 0)
            return a;
        return findGreatestDivisor(b, a % b);
    }

    static String convertToFraction(double num) {
        // Divide number into two parts
        double intPart = Math.floor(num);
        double fracPart = num - intPart;

        // Creating precision value for transformation the fractional part to the integer part
        long precValue = 1000000000;
        long greatDiv = findGreatestDivisor(Math.round(fracPart * precValue), precValue);

        long nominator = Math.round(fracPart * precValue) / greatDiv;
        long denominator = precValue / greatDiv;

        return (long) intPart * denominator + nominator + "/" + denominator;
    }

    private static String[] changeSlackToX(String[] rowsAndColsNames, double[][] simplexMatrix) {
        int[] operable_Row_Column = findOperableRowAndColumn(simplexMatrix);
        rowsAndColsNames[operable_Row_Column[0]] = rowsAndColsNames[simplexMatrix.length + operable_Row_Column[1]];
        return rowsAndColsNames;
    }

    private static double[][] matrixTransformation(double[][] simplexMatrix) {
        int[] operable_Row_Column = findOperableRowAndColumn(simplexMatrix);
        for (int i = 0; i < simplexMatrix.length; i++) {
            if (i != operable_Row_Column[0]) {
                for (int j = 0; j < simplexMatrix[i].length; j++) {
                    if (j != operable_Row_Column[1]) {
                        simplexMatrix[i][j] = -(simplexMatrix[i][operable_Row_Column[1]] /
                                simplexMatrix[operable_Row_Column[0]][operable_Row_Column[1]])
                                * simplexMatrix[operable_Row_Column[0]][j] + simplexMatrix[i][j];
                    }
                }
                simplexMatrix[i][operable_Row_Column[1]] = 0;
            }
        }
        return simplexMatrix;
    }

    private static boolean negativeSignCheck(double[] z) {
        boolean check = false;
        for (double i : z)
            if (i > 0) {
                check = true;
                break;
            }
        return check;
    }

    static double convertFractionNum(String numberStr) {
        StringBuilder numPart = new StringBuilder();
        double numerator, denominator;
        boolean flag = false;
        try {
            // Check if element has '/' character
            for (int n = 0; n < numberStr.length(); n++)
                if (numberStr.charAt(n) == '/') {
                    flag = true;
                    break;
                }

            // If it has, convert it to double value
            if (flag) {
                int k = 0;
                for (; numberStr.charAt(k) != '/'; k++)
                    numPart.append(numberStr.charAt(k));
                numerator = Double.parseDouble(numPart.toString());
                k++;
                numPart = new StringBuilder();
                for (; k < numberStr.length(); k++)
                    numPart.append(numberStr.charAt(k));
                denominator = Double.parseDouble(numPart.toString());
                return numerator / denominator;
            } else
                return Double.parseDouble(numberStr);
        }
        catch (Exception e) {
            System.out.println("ERROR: incorrect input data");
            System.out.print("\nPress enter to continue...");
            Scanner pressEnterSc = new Scanner(System.in);
            String pressEnter = pressEnterSc.nextLine();
            System.exit(1);
            return 0;
        }
    }

    private static double[][] readFile(String fileName) {
        double[][] simplexMatrix = new double[0][0];
        try {
            Scanner reader = new Scanner(new File(fileName));

            int rows = 0;
            int firstLineColumns = 0;
            String firstLine;

            // Calculating the sizes of simpleMatrix
            firstLine = reader.nextLine();
            rows++;
            while (reader.hasNextLine()) {
                rows++;
                reader.nextLine();
            }
            reader.close();
            rows -= 3;

            for (int i = 0; i < firstLine.length(); i++) {
                if (firstLine.charAt(i) == 9) {
                    firstLineColumns++;
                }
            }
            firstLineColumns++;

            simplexMatrix = new double[rows][firstLineColumns + rows];

            // Filling matrix with zeros
            for (int i = 0; i < simplexMatrix.length; i++) {
                for (int j = 0; j < simplexMatrix[0].length; j++) {
                    simplexMatrix[i][j] = 0;
                }
            }

            // Filling matrix with values from input file
            reader = new Scanner(new File(fileName));
            for (int j = 0; j < firstLineColumns; j++) {
                simplexMatrix[rows - 1][j] = convertFractionNum(reader.next());
            }

            for (int i = 0; i < rows - 1; i++) {
                for (int j = 0; j < firstLineColumns; j++) {
                    simplexMatrix[i][j] = convertFractionNum(reader.next());
                }
            }

            for (int i = 0; i < rows - 1; i++) {
                simplexMatrix[i][firstLineColumns + rows - 1] = convertFractionNum(reader.next());
            }
            reader.close();

            for (int i = 0; i < simplexMatrix.length - 1; i++) {
                if (simplexMatrix[i][simplexMatrix[0].length - 1] < 0) {
                    System.out.println("ERROR: incorrect problem statement");
                    System.out.print("\nPress enter to continue...");
                    Scanner pressEnterSc = new Scanner(System.in);
                    String pressEnter = pressEnterSc.nextLine();
                    System.exit(1);
                }
            }

            // Slack values (ones) filling
            int i = 0;
            for (int j = firstLineColumns; j < firstLineColumns + rows - 1; j++) {
                simplexMatrix[i][j] = 1;
                i++;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return simplexMatrix;
    }

    private static void writeFile(double[][] simplexMatrix, String[] rowsAndColsNames) {
        String[] simplexMatrixStr = new String[simplexMatrix.length + 1];
        DecimalFormat format = new DecimalFormat("0.####");

        try {
            File checkFileExistence = new File("output.txt");
            if (checkFileExistence.exists())
                simplexMatrixStr[0] = "\n";

            FileWriter writer = new FileWriter("output.txt", checkFileExistence.exists());
            if (simplexMatrixStr[0] == null) {
                simplexMatrixStr[0] = '\t' + rowsAndColsNames[simplexMatrix.length];
                for (int i = simplexMatrix.length + 1; i < simplexMatrix[0].length + simplexMatrix.length; i++)
                    simplexMatrixStr[0] += '\t' + rowsAndColsNames[i];
            } else
                for (int i = simplexMatrix.length; i < simplexMatrix[0].length + simplexMatrix.length; i++)
                    simplexMatrixStr[0] += '\t' + rowsAndColsNames[i];

            for (int i = 0; i < simplexMatrix.length; i++) {
                simplexMatrixStr[i + 1] = rowsAndColsNames[i] + "\t";
                for (int j = 0; j < simplexMatrix[simplexMatrix.length - 1].length; j++) {
                    if (i == simplexMatrix.length - 1 && simplexMatrix[i][j] != 0)
                        if (simplexMatrix[i][j] == (int) simplexMatrix[i][j])
                            simplexMatrixStr[i + 1] += format.format(simplexMatrix[i][j] * -1) + "\t";
                        else
                            simplexMatrixStr[i + 1] += convertToFraction((simplexMatrix[i][j]) * -1) + "\t";
                    else if (simplexMatrix[i][j] == (int) simplexMatrix[i][j])
                        simplexMatrixStr[i + 1] += format.format(simplexMatrix[i][j]) + "\t";
                    else
                        simplexMatrixStr[i + 1] += convertToFraction((simplexMatrix[i][j])) + "\t";
                }
            }
            for (String s : simplexMatrixStr)
                writer.write(s + '\n');
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String[] createRowsColsNames(int rows, int cols) {
        String[] rowsColsNames = new String[rows + cols];
        for (int i = 0; i < rowsColsNames.length; i++) {
            if (i < rows) {
                if (i == rows - 1)
                    rowsColsNames[i] = "z";
                else
                    rowsColsNames[i] = "s" + (i + 1);
            }
            if (i >= rows) {
                if (i == rowsColsNames.length - 1)
                    rowsColsNames[i] = "b";
                else if (i < rowsColsNames.length - rows)
                    rowsColsNames[i] = "x" + (i + 1 - rows);
                else
                    rowsColsNames[i] = "s" + (i + 1 - cols);
            }
        }

        return rowsColsNames;
    }

    private static double[][] divX(double[][] simplexMatrix, String[] rowsColsNames) {
        // Divide all elements of row on X to X value to get one of the X* value
        int tempInd = 1;
        while (rowsColsNames[tempInd].charAt(0) != 'z')
            tempInd++;
        tempInd++;
        for (int i = 0; i < tempInd; i++)
            if (rowsColsNames[i].charAt(0) == 'x') {
                int index = 0;
                for (int k = tempInd; k < rowsColsNames.length; k++)
                    if (Objects.equals(rowsColsNames[k], rowsColsNames[i]))
                        index = k - tempInd;

                double tempElem = simplexMatrix[i][index];
                for (int j = 0; j < simplexMatrix[0].length; j++) {
                    simplexMatrix[i][j] = simplexMatrix[i][j] / tempElem;
                }
            }
        return simplexMatrix;
    }

    private static boolean isFeasSol(double[][] simplexMatrix) {
        boolean isFeasReg = true;
        boolean temp = true;

        // Check if in the column of constraints would not be not positive values
        for (int j = 0; j < simplexMatrix[0].length - 1; j++) {
            for (int i = 0; i < simplexMatrix.length - 1; i++) {
                if (simplexMatrix[i][j] > 0) {
                    temp = true;
                    break;
                } else
                    temp = false;

            }
            isFeasReg = temp;
            if (!isFeasReg)
                break;
        }
        return isFeasReg;
    }

    private static double[] getX_Star(double[][] simplexMatrix, String[] rowsColsNames) {
        double[] xStar = new double[simplexMatrix[0].length - 1];

        int temp = 0;
        for (int i = simplexMatrix.length; i < simplexMatrix[0].length - 1; i++)
            temp++;
        temp += 2;

        for (int i = 0; i < simplexMatrix.length - 1; i++) {
            if (rowsColsNames[i].charAt(0) == 'x') {
                xStar[Character.getNumericValue(rowsColsNames[i].charAt(1)) - 1] =
                        simplexMatrix[i][simplexMatrix[0].length - 1];
            } else if (rowsColsNames[i].charAt(0) == 's') {
                xStar[Character.getNumericValue(rowsColsNames[i].charAt(1)) - 1 + temp - 1] =
                        simplexMatrix[i][simplexMatrix[0].length - 1];
            }
        }
        return xStar;
    }

    public static void startProgram(String fileName) throws IOException {
        boolean noFeasSol = false;
        double[][] simplexMatrix = readFile(fileName);
        String[] rowsAndColsNames = createRowsColsNames(simplexMatrix.length, simplexMatrix[0].length);

        writeFile(simplexMatrix, rowsAndColsNames);
        if (!isFeasSol(simplexMatrix)) {
            noFeasSol = true;
        }
        while (negativeSignCheck(simplexMatrix[simplexMatrix.length - 1]) && !noFeasSol) {
            rowsAndColsNames = changeSlackToX(rowsAndColsNames, simplexMatrix);
            simplexMatrix = matrixTransformation(simplexMatrix);
            simplexMatrix = divX(simplexMatrix, rowsAndColsNames);
            writeFile(simplexMatrix, rowsAndColsNames);
            if (!isFeasSol(simplexMatrix))
                noFeasSol = true;
        }

        while (true) {
            if (noFeasSol) {
                FileWriter writer = new FileWriter("output.txt", true);
                writer.write('\n' + "SOLUTION FOUND: unbounded problem\n");
                writer.write('\n');
                writer.close();
                break;
            }

            FileWriter writer = new FileWriter("output.txt", true);
            writer.write('\n' + "SOLUTION FOUND: unique solution");
            if (simplexMatrix[simplexMatrix.length - 1][simplexMatrix[0].length - 1] ==
                    (int) simplexMatrix[simplexMatrix.length - 1][simplexMatrix[0].length - 1])
                writer.write('\n' + "Objective: z = " +
                        ((int) simplexMatrix[simplexMatrix.length - 1][simplexMatrix[0].length - 1]) * -1);
            else
                writer.write('\n' + "Objective: z = " +
                        convertToFraction((simplexMatrix[simplexMatrix.length - 1]
                                [simplexMatrix[0].length - 1]) * -1));

            writer.write('\n');

            DecimalFormat format = new DecimalFormat("0.####");
            double[] xStar = getX_Star(simplexMatrix, rowsAndColsNames);
            for (double i : xStar)
                if (i == (int) i)
                    writer.write(format.format(i) + '\t');
                else
                    writer.write(convertToFraction(i) + '\t');

            writer.write('\n');
            writer.close();
            break;
        }
    }


    public static void main(String[] args) throws IOException {
        startProgram("input.txt");
    }
}