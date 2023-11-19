import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Q1 {
    public static void main(String[] args) {
        try {
            // Initialize a random number generator with seed 244
            Random random = new Random(244);

            // Initialize arrays to hold the start positions, sizes of 3 groups and the pairs
            int[] groupStarts = new int[3];
            int[] groupSizes = new int[3];
            List<double[]> groupPairs[] = new ArrayList[3];
            for (int i = 0; i < 3; i++) {
                groupPairs[i] = new ArrayList<>();
            }

            // Generate pairs until both random numbers are greater than 0.91
            while(true) {
                double d1 = random.nextDouble();
                double d2 = random.nextDouble();
                if (d1 > 0.91 && d2 > 0.91) {
                    break;
                }

                // Grouping
                int groupIndex = d1 < 0.46 && d2 < 0.46 ? 0 : d1 > 0.72 && d2 > 0.72 ? 2 : 1;
                groupPairs[groupIndex].add(new double[] {d1, d2});
                // Update the size of the group
                groupSizes[groupIndex]++;
            }

            // Set the start positions based on group sizes
            // Ensure that each group starts at a position that is a multiple of 32
            groupStarts[0] = 32;
            groupStarts[1] = groupStarts[0] + (groupSizes[0] % 2 == 0 ? groupSizes[0] * 16 : (groupSizes[0] + 1) * 16);
            groupStarts[2] = groupStarts[1] + (groupSizes[1] % 2 == 0 ? groupSizes[1] * 16 : (groupSizes[1] + 1) * 16);

            // Open a file named h2q1.dat for writing
            long genStart = System.nanoTime();
            RandomAccessFile file = new RandomAccessFile("h2q1.dat","rw");

            // Write the total size and group sizes to the file
            int totalSize = groupSizes[0] + groupSizes[1] + groupSizes[2];
            file.writeInt(totalSize);
            for (int i = 0; i < 3; i++) {
                file.writeInt(groupSizes[i]);
                file.writeInt(groupStarts[i]);
            }
            file.writeInt(0);  // Add 4 extra bytes

            // Write each group's pairs to the file
            for (int i = 0; i < 3; i++) {
                for (double[] pair : groupPairs[i]) {
                    ByteBuffer buffer = ByteBuffer.allocate(16);
                    buffer.putDouble(pair[0]);
                    buffer.putDouble(pair[1]);
                    file.write(buffer.array());
                }
            }

            file.close();

            long genEnd = System.nanoTime();

            System.out.println("Total size: " + totalSize);
            System.out.println("Group sizes: " + groupSizes[0] + ", " + groupSizes[1] + ", " + groupSizes[2]);
            System.out.println("Generation time: " + (genEnd - genStart) + " ns");

            // Read the file and count
            long readStart = System.nanoTime();

            RandomAccessFile readFile = new RandomAccessFile("h2q1.dat", "r");

            readFile.readInt();  // Read totalSize
            for (int i = 0; i < 3; i++) {
                groupSizes[i] = readFile.readInt();
                groupStarts[i] = readFile.readInt();
            }
            readFile.readInt();  // Read 4 extra bytes

            int oneOverHalf = 0;
            int pairOverHalf = 0;

            for (int i = 1; i < 3; i++) {  // Start from 1, not 0
                readFile.seek(groupStarts[i]);
                for (int j = 0; j < groupSizes[i]; j++) {
                    byte[] bytes = new byte[16];
                    readFile.read(bytes);
                    ByteBuffer buffer = ByteBuffer.wrap(bytes);
                    double d1 = buffer.getDouble();
                    double d2 = buffer.getDouble();
                    if (d1 > 0.5 || d2 > 0.5) {
                        oneOverHalf++;
                    }
                    if (d1 > 0.5 && d2 > 0.5) {
                        pairOverHalf++;
                    }
                }
            }

            readFile.close();

            long readEnd = System.nanoTime();

            System.out.println("One over half: " + oneOverHalf);
            System.out.println("Pair over half:" + pairOverHalf);
            System.out.println("Reading and counting time: " + (readEnd - readStart) + " ns");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}