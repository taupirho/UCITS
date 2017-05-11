

    package ucits;
    import java.util.ArrayList;
    public class RecordGroup {
        private ArrayList indexList;
        private double thetaSquared;
        public RecordGroup(ArrayList recList, double theta) {
            thetaSquared = theta;
            indexList = recList;
        }
        public double getThetaSquared() {
            return thetaSquared;
        }
        public void setThetaSquared(double thetaSquared) {
            this.thetaSquared = thetaSquared;
        }
        public ArrayList getIndexList() {
            return indexList;
        }
        public void setIndexList(ArrayList sIndex) {
            indexList = sIndex;
        }
    }
