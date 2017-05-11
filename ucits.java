    package ucits;
    import java.io.BufferedReader;
    import java.io.IOException;
    import java.io.InputStreamReader;
    import java.sql.Connection;
    import java.sql.DriverManager;
    import java.sql.PreparedStatement;
    import java.sql.ResultSet;
    import java.sql.SQLException;
    import java.sql.Statement;
    import java.text.ParseException;
    import java.text.SimpleDateFormat;
    import java.util.ArrayList;
    import java.util.Date;

    public class ucits {

        public static final String DBURL = "jdbc:oracle:thin:@your_ip:your_dbname";
        public static final String DBUSER = "your_username";
        public static final String DBPASS = ""
        your_passwword
        public static final int CCL = 36;
        public static final double CT = 4.5;
        public static final int ICL = 9;
        public static String errorMessage = "";
        public static String source_pmnem = "";
        public static String source_ic_class = "";
        public static String ucits_pmnem = "";
        public static String ucits_ic_class = "";
        public static String ic_class = "";
        public static String area_mnem = "";
        public static String start_date = "";
        public static String p_enddate;
        public static int rowCount = 0;
        public static double mcap_tot = 0.0;
        public static int index = -1;

        public static boolean isValidDate(String date) {
            // set date format, this can be changed to whatever format
            // you want, MM-dd-yyyy, MM.dd.yyyy, dd.MM.yyyy etc.
            // you can read more about it here:
            // http://java.sun.com/j2se/1.4.2/docs/api/index.html

            SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy");
            sdf.setLenient(false); // strict checking in force

            // declare and initialize testDate variable, this is what will hold
            // our converted string

            Date testDate = null;
            // we will now try to parse the string into date form    
            try {
                testDate = sdf.parse(date);
                //System.out.println("testDate =  : "+testDate);
            }
            // if the format of the string provided doesn't match the format we
            // declared in SimpleDateFormat() we will get an exception
            catch (ParseException e) {
                return false;
            }
            return true;
        }

        // This is where we retrieve our main data set and store into an array of 
        //Record types. This data structure will be our initial "portfolio" which we then 
        // process later to see if we can construct a UCITS III compliant portfolio from it.
        //
        // We also get some other data that is used later on in the program.

        public static void get_data(String s, String source_pmnem, ArrayList recList) throws SQLException {
                String smnem; // Index entity mnemonic
                double mcap; // Index market cap
                int nr_rows = 0;
                //   	 BufferedReader br = new BufferedReader(
                //          new   InputStreamReader(System.in));
                //   	 String cbuf;
                //   	 System.out.print("Enter required date in dd-mmm-yyyy format: ");
                //   	 
                //      String s = br.readLine();

                if (!isValidDate(s)) {
                    System.out.println("Date entered is invalid");
                    System.exit(0);
                } else {

                    // build SQL query
                    // Obviously these details are specific to my system but essentially 
                    // you want a list of index member identifiers and their market caps ordered by market cap
                    String q = "";
                    q += "select  i.s_parent,  sum(capel.market_cap_in_currency_qt2(i.s_mnem,to_date('" + s + "')";
                    q += "-1,to_date('" + s + "'),pc.c_mnem,ps.shs_source)/ic_factor) ffmc ";
                    q += "from p_curr pc, p_shs_source ps, special_areas a, indx_class i, p_foliolu p ";
                    q += "where   pc.c_startdate <= to_date('" + s + "') ";
                    q += "and     (ps.shs_enddate is null or ps.shs_enddate >= to_date('" + s + "')) ";
                    q += "and     (pc.c_enddate is null or pc.c_enddate >= to_date('" + s + "')) ";
                    q += "and     (i.ic_enddate is null or i.ic_enddate >= to_date('" + s + "')) ";
                    q += "and     i.ic_startdate <= to_date('" + s + "') ";
                    q += "and     ps.shs_startdate <= to_date('" + s + "') ";
                    q += "and     pc.p_mnem = p.p_mnem ";
                    q += "and     ps.p_mnem = p.p_mnem ";
                    q += "and     a.mrkt_mnem = i.ic_mrkt and  a.area_mnem = nvl(p.area_mnem,'WORLD') ";
                    q += "and     i.ic_class = p.ic_class and  (i.ic_grp = p.ic_grp or p.ic_grp is null) ";
                    q += "and     (i.ic_sec = p.ic_sec or p.ic_sec is null)    and (i.ic_sub = p.ic_sub or p.ic_sub is null) ";
                    q += "and     p.p_mnem = '" + source_pmnem + "' group by i.s_parent order by 2 desc";

                    // System.out.println("query = " + q);
                    // Load Oracle JDBC Driver;
                    DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
                    // Connect to Oracle Database

                    Connection con = DriverManager.getConnection(DBURL, DBUSER, DBPASS);
                    Statement statement = con.createStatement();
                    ResultSet rs = statement.executeQuery(q);
                    while (rs.next()) {
                        smnem = rs.getString(1);
                        mcap = rs.getDouble(2);
                        Record Rec = new Record(smnem, mcap);
                        recList.add(Rec);
                        mcap_tot = mcap_tot + mcap;
                        rowCount++;
                    }
                    if (rowCount > 0)
                        rs.close();
                    else {
                        System.out.println("No data found ... exiting");
                        System.exit(0);
                    }
                    // from here to *********** IGNORE bit below is stuff specific to my system                                                      
                    //  and not required for the calc
                    q = "";
                    q = "select ic_class from p_foliolu where p_mnem = upper('" + source_pmnem + "')";

                    rs = statement.executeQuery(q);
                    nr_rows = 0;
                    while (rs.next()) {
                        source_ic_class = rs.getString(1).toUpperCase();
                        nr_rows++;
                    }
                    if (nr_rows > 0)
                        rs.close();
                    else if (nr_rows > 2) {
                        System.out.println("More than 1 P_FOLIOLU record found for mnemonic " + source_pmnem + " ...exiting");
                        System.exit(0);
                    }
                    q = "";
                    q = q + "select area_mnem,ic_class from p_foliolu where p_mnem = '" + ucits_pmnem + "'";
                    rs = statement.executeQuery(q);
                    nr_rows = 0;
                    while (rs.next()) {
                        area_mnem = rs.getString(1).toUpperCase();
                        ic_class = rs.getString(2).toUpperCase();
                        nr_rows++;
                    }
                    if (nr_rows > 0)
                        rs.close();
                    else if (nr_rows > 2) {
                        System.out.println("More than 1 P_FOLIOLU record found for mnemonic " + ucits_pmnem + " ...exiting");
                        System.exit(0);
                    }
                    if (!(ic_class == null || ic_class.trim().equals(""))) {
                        ucits_ic_class = ic_class;
                    }

                    q = "";
                    q = q + "select to_date('" + start_date + "','DD-MON-RR')-decode(to_char(to_date('" + start_date + "','DD-MON-RR'),'DY'),'MON',3,'SUN',2,1) from dual";
                    rs = statement.executeQuery(q);
                    nr_rows = 0;
                    while (rs.next()) {
                        p_enddate = rs.getString(1).substring(0, 10);
                        nr_rows++;
                    }
                    if (nr_rows > 0)
                        rs.close();
                    statement.close();
                    con.close();

                    // ***************** IGNORE ABOVE **************

                    System.out.println("Portfolio records retrieved  = " + rowCount);
                    if (rowCount > 0) {
                        System.out.println("Original pfolio");
                        System.out.println("---------------");
                        System.out.println("SMNEM\t| Weight");
                        System.out.println("-----\t|-------");
                        // Set initial weights based on mcap
                        for (int i = 0; i < recList.size(); i++) {
                            Record rec = (Record) recList.get(i);
                            rec.SetWgt((rec.GetMcap() / mcap_tot) * 100);
                            rec.SetFixedWgt(rec.GetWgt());
                            System.out.println(rec.GetSmnem() + "\t|" + rec.GetWgt());
                        }
                        System.out.println();
                    }
                }
            }
            // Checks a portfolio for UCITS compliance
            // Type = I indicates we just want to check initial pfolio 
            // for UCITS compliance  i.e no need to calculate theta.
            // Returns < 0  if pfolio is not UCITS compliance
        public static double check_index(ArrayList recList, char type) {
                double ul = 0;
                double tot = 0;
                double wgt = 0.0;
                double theta = 0;
                double orig_wgt = 0.0;
                double final_wgt = 0.0;

                for (int i = 0; i < recList.size(); i++) {
                    Record rec = (Record) recList.get(i);
                    char id = rec.GetId();
                    orig_wgt = rec.GetWgt();
                    final_wgt = rec.GetFinalWgt();
                    if (type == 'I')
                        wgt = orig_wgt;
                    else
                        wgt = final_wgt;
                    tot += wgt;
                    // max weight of any one stock <= 9
                    if (wgt > ICL) {
                        return -1;
                    }

                    // No short positions
                    if (wgt < 0) {
                        return -2;
                    }
                    if (wgt > CT) // > 4.5%
                    {
                        ul += ul;
                    };
                    if (id == '9' && wgt != ICL)
                        return -4;
                    if (id == 'N' && wgt != CT)
                        return -5;
                    if (id == 'H' && wgt <= CT)
                        return -6;
                    // theta is a measure of how different the new portfolio is from our original
                    // We want this to be a minimum
                    theta = theta + (final_wgt - orig_wgt) * (final_wgt - orig_wgt);
                }
                // the sum of all weights > 4.5 must be <= CCL
                if (ul > CCL || (tot > 100.0001 || tot < 99.9999)) {
                    return -3;
                }
                if (type == 'I')
                    return 0;
                return theta;
            }
            // Print out a portfolio
        public static void print_index(ArrayList recList) {
            System.out.println("SMNEM\t|Original Weight\t|UCITS Weight\t|UCITS factor");
            System.out.println("-----\t|---------------\t|------------\t|------------");

            for (int i = 0; i < recList.size(); i++) {
                Record rec = (Record) recList.get(i);
                System.out.println(rec.GetSmnem() + "\t|" + rec.GetWgt() + "\t|" + rec.GetFinalWgt() + "\t|" + rec.GetWgt() / rec.GetFinalWgt());
            }
        }

        //
        // This is our main procedure to calculate a UCITS III compliant pfolio.
        // Basically we set 3 pivot points -  CAP Pivot (wgt = 9%), HIGH Pivot (wgt = 4.5%)
        // and LOW pivot (wgts < 4.5%) - on the array of our original pfolio records. Starting at   
        // record 1 we then iterate through all valid combinations of the above pivot points, adjusting 
        // the pfolio weights to create a new synthetic pfolio. At the end of each iteration we 
        // test the resulting new pfolio to see whether it’s UCITS compliant or not.
        // If not we do the next iteration, if yes we store the new pfolio and go on to the 
        // next iteration. When all iterations are complete we cycle through any saved UCITS 
        // compliant pfolios and calculate which one has the lowest tracking error between it 
        // and the original non-compliant pfolio. The lowest becomes our new UCITS compliant 
        // pfolio
        //
        public static void iterate_weights(ArrayList recList, ArrayList indexList) {
            int cap_pivot = 0;
            int high_pivot = 0;
            int low_pivot = 0;
            int n = recList.size();
            int k1 = 0; // nr caps = 9
            int k2 = 0; // nr high caps ( > 4.5 & < 9 )
            if (check_index(recList, 'I') < 0) {
                for (k1 = 0; k1 <= 4; k1++) {
                    cap_pivot = k1;

                    for (k2 = 0; k2 <= (8 - 2 * k1); k2++) {
                        if (cap_pivot == 0) {
                            high_pivot = 0;
                        } else {
                            high_pivot = cap_pivot + k2 + 1;
                        }
                        for (int i = 1; i <= n; i++) {
                            double hilo_caps = 0;
                            double hi_caps = 0;
                            double lo_caps = 0;
                            double nine_caps = 0.0;
                            double sum_fixed = 0;
                            double sum_ccl = 0.0;
                            double factor = 0;
                            double lo_cap_factor = 0.0;
                            double hi_cap_factor = 0.0;
                            low_pivot = Math.max(i, high_pivot);
                            // Assign 9% weights
                            for (int l1 = 1; l1 <= cap_pivot; l1++) {
                                Record rec = (Record) recList.get(l1 - 1);
                                rec.SetFixedWgt(ICL);
                                rec.SetId('9');
                            }
                            // Assign high cap weights
                            for (int l2 = cap_pivot + 1; l2 < high_pivot; l2++) {
                                Record rec = (Record) recList.get(l2 - 1);
                                rec.SetFixedWgt(rec.GetWgt());
                                rec.SetId('H');
                            }
                            // Assign 4.5% weights
                            for (int l3 = Math.max(high_pivot, 1); l3 <= low_pivot; l3++) {
                                Record rec = (Record) recList.get(l3 - 1);
                                rec.SetFixedWgt(CT);
                                rec.SetId('N');
                            }
                            if (high_pivot == low_pivot) {
                                for (int l4 = low_pivot; l4 <= n; l4++) {
                                    Record rec = (Record) recList.get(l4 - 1);
                                    rec.SetFixedWgt(rec.GetWgt());
                                    rec.SetId('L');
                                }
                            } else {
                                // Assign low caps
                                for (int l4 = low_pivot + 1; l4 <= n; l4++) {
                                    Record rec = (Record) recList.get(l4 - 1);
                                    rec.SetFixedWgt(rec.GetWgt());
                                    rec.SetId('L');
                                }
                            }
                            // When we've got to here we have done 1 iteration of setting the CAP, 
                            // HIGH and LOW pivots
                            // And we have set our fixed 4.5% and 9% weights if any.
                            // We now calculate the factor to be applied to any HIGH & LOW cap weights
                            // i.e the factor is applied to any wgts > 4.5 &&  < 9 or those < 4.5
                            //
                            // factor =  1+( (100-SUM(NEW WGTS))/SUM(HIGH/LOW CAPS))
                            for (int y = 1; y <= n; y++) {
                                Record rec4 = (Record) recList.get(y - 1);
                                sum_fixed += rec4.GetFixedWgt();
                                if (rec4.GetId() == 'L') {
                                    lo_caps += rec4.GetFixedWgt();
                                }
                                if (rec4.GetId() == 'H') {
                                    hi_caps += rec4.GetFixedWgt();
                                }
                            }
                            hilo_caps = lo_caps + hi_caps;
                            factor = 1 + ((100 - sum_fixed) / hilo_caps);

                            // Next we apply this factor to all HIGH & LOW CAP fixed weights
                            // to give us a set of allocated weights
                            hi_caps = 0;
                            lo_caps = 0;
                            nine_caps = 0;

                            // In this loop we  apply our factor. Also check for CCL (36% threshold) being 
                            // overweight and if it 
                            // is we move the overweight
                            // portion from the high caps to the low caps
                            for (int t = 1; t <= n; t++) {
                                Record rec4 = (Record) recList.get(t - 1);
                                if (rec4.GetId() == 'H' || rec4.GetId() == 'L') {
                                    rec4.SetAllocWgt(rec4.GetFixedWgt() * factor);
                                } else {
                                    rec4.SetAllocWgt(rec4.GetFixedWgt());
                                }
                                rec4.SetFinalWgt(rec4.GetAllocWgt());
                                // Calculate some intermediate results for next step

                                if (rec4.GetId() == '9') {
                                    nine_caps += rec4.GetAllocWgt();
                                }
                                if (rec4.GetId() == 'L') {
                                    lo_caps += rec4.GetAllocWgt();
                                }
                                if (rec4.GetId() == 'H') {
                                    hi_caps += rec4.GetAllocWgt();
                                }
                                // Allocate any CCL (36% threshold) overweight to low caps
                                sum_ccl = hi_caps + nine_caps;
                                if (sum_ccl > CCL) {
                                    hi_cap_factor = 1 - (sum_ccl - CCL) / hi_caps;
                                    lo_cap_factor = 1 + (sum_ccl - CCL) / lo_caps;
                                    if (rec4.GetId() == 'H') {
                                        rec4.SetFinalWgt(rec4.GetAllocWgt() * hi_cap_factor);
                                    }
                                    if (rec4.GetId() == 'L') {
                                        rec4.SetFinalWgt(rec4.GetAllocWgt() * lo_cap_factor);
                                    }
                                }
                            }
                            // Check pfolio for UCITS compliance ( returns > 0 if yes)
                            // If it is store it so we can check which one is optimal
                            double chk = check_index(recList, 'F');
                            if (chk > 0) {
                                store_pfolios(recList, chk, indexList);
                            }
                        }
                    }
                }
            } else {
                System.out.println("Index already UCITS compliant");
                System.exit(0);
            }
        }
        public void prompt() throws IOException {
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            String s = br.readLine();
        }

        // Store UCITS pfolio in temp collection data structure
        // We have to make a copy of the pfolio before storing as JAVA passes everything by REF
        // This prevents subsequent changes affecting our collection of stored pfolios
        public static void store_pfolios(ArrayList recList, double theta, ArrayList indexList) {

                ArrayList recList2 = new ArrayList();
                for (int x = 0; x < recList.size(); x++) {
                    Record rec5 = new Record();
                    Record rec6 = (Record) recList.get(x);
                    rec5.SetFactor(rec6.GetFactor());
                    rec5.SetFinalWgt(rec6.GetFinalWgt());
                    rec5.SetFixedWgt(rec6.GetFixedWgt());
                    rec5.SetAllocWgt(rec6.GetAllocWgt());
                    rec5.SetFixing(rec6.GetFixing());
                    rec5.SetId(rec6.GetId());
                    rec5.SetWgt(rec6.GetWgt());
                    rec5.SetSmnem(rec6.GetSmnem());
                    rec5.SetMcap(rec6.GetMcap());

                    recList2.add(rec5);
                }
                RecordGroup rGroup = new RecordGroup(recList2, theta);
                indexList.add(rGroup);
            }
            // Iterate through our stored list of UCITS compliant pfolios, calculate the 
            // optimal one and store its position in the data structure (index variable)
            // and print it out.
        public static void get_optimal_pfolio(ArrayList iList) {
                ArrayList rList = new ArrayList();
                double smallestTheta = 100000000.0;
                index = -1;
                for (int i = 0; i < iList.size(); i++) {
                    RecordGroup rGroup = (RecordGroup) iList.get(i);
                    if (rGroup.getThetaSquared() < smallestTheta) {
                        smallestTheta = rGroup.getThetaSquared();
                        rList = rGroup.getIndexList();
                        index = i;
                        //print_index(rList);
                    }
                }
                if (index == -1) {
                    System.out.println("No UCITS compliant pfolio found");
                    return;
                }
                System.out.println("Smallest Theta = " + smallestTheta + ", Index = " + index);
                System.out.println("Optimal UCITS compliant pfolio");
                System.out.println("------------------------------");
                print_index(rList);
            }
            // You can IGNORE this function as it’s specific to my system
            // and stores UCITS portfolio details in a database
            //
            // Insert our optimal pfolio into the database
            //
        public static void store_optimal_pfolio(int pos, ArrayList iList) throws SQLException {
            DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
            // Connect to Oracle Database
            Connection con = DriverManager.getConnection(DBURL, DBUSER, DBPASS);
            PreparedStatement preparedStatement = con.prepareStatement("delete from indx_class where ic_class = ? and ic_startdate >= to_date(?,'DD-MON-RR')");
            preparedStatement.setString(1, ucits_ic_class);
            preparedStatement.setString(2, start_date);
            int deleteCount = preparedStatement.executeUpdate();
            con.close();
            RecordGroup rGroup = (RecordGroup) iList.get(pos);
            ArrayList recs = rGroup.getIndexList();
            con = DriverManager.getConnection(DBURL, DBUSER, DBPASS);
            System.out.println();
            System.out.println("Writing UCITS pfolio to the database");
            System.out.println();
            for (int i = 0; i < recs.size(); i++) {
                Record pfolio = (Record) recs.get(i);
                double orig_wgt = pfolio.GetWgt();
                double new_wgt = pfolio.GetFinalWgt();
                double pfactor = orig_wgt / new_wgt;
                String smnem = pfolio.GetSmnem();
                preparedStatement = con.prepareStatement(
                    "insert into indx_class i1 (ic_class,ic_grp,ic_sec,ic_sub,ic_mrkt,s_mnem,s_parent,ic_startdate,ic_enddate,ic_factor,change_date) " +
                    "select ?,ic_grp,ic_sec,ic_sub,ic_mrkt,s_mnem,s_parent,to_date(?,'DD-MON-RR'),ic_enddate,?*ic_factor,sysdate " +
                    "from indx_class i2 where i2.ic_class = upper(?) and i2.ic_startdate <= to_date(?,'DD-MON-RR') " +
                    "and (i2.ic_enddate is null or i2.ic_enddate >= to_date(?,'DD-MON-RR')) " +
                    "and s_parent = ?"
                );
                preparedStatement.setString(1, ucits_ic_class);
                preparedStatement.setString(2, start_date);
                preparedStatement.setDouble(3, pfactor);
                preparedStatement.setString(4, source_ic_class);
                preparedStatement.setString(5, start_date);
                preparedStatement.setString(6, start_date);
                preparedStatement.setString(7, smnem);
                int insertCount = preparedStatement.executeUpdate();
                preparedStatement = con.prepareStatement(
                    "update indx_class set ic_enddate = to_date(?,'YYYY-MM-DD') where ic_class = ? and ic_startdate <= to_date(?,'YYYY-MM-DD') " +
                    "and (ic_enddate is null or ic_enddate >=  to_date(?,'YYYY-MM-DD')) and s_parent = ? "
                );
                preparedStatement.setString(1, p_enddate);
                preparedStatement.setString(2, ucits_ic_class);
                preparedStatement.setString(3, p_enddate);
                preparedStatement.setString(4, p_enddate);
                preparedStatement.setString(5, smnem);
                int updateCount = preparedStatement.executeUpdate();
                System.out.println("Smnem:" + smnem + "\t|factor applied: " + pfactor);
            }
            con.close();
            con = DriverManager.getConnection(DBURL, DBUSER, DBPASS);


            // The below prepared statement should look something like this and is done to ensure any stocks
            // that came out of the source index GLOBMIN in the latest rebalaance
            // get their ic_enddate set properly in the new UCITS index
            //
            // set i.ic_enddate =
            // (
            //    select ic_enddate
            //    from indx_class where ic_class = 'GL'
            //    and ic_enddate is not null
            //    and s_mnem = i.s_mnem
            //    and trunc(ic_startdate) = trunc(i.ic_startdate)
            // )
            // where ic_class = '19'
            // and ic_enddate is null
            // and ic_startdate <= '19-jun-15'

            System.out.println("Updating UCITS end dates for any stocks removed in latest rebalalance of source indx:" + source_ic_class);
            preparedStatement = con.prepareStatement(
                "update indx_class i set i.ic_enddate = ( select ic_enddate from indx_class  where ic_class = ? " +
                "and ic_enddate is not null and s_mnem = i.s_mnem and trunc(ic_startdate) = trunc(i.ic_startdate) ) " +
                "where ic_class = ? and ic_enddate is null and ic_startdate <= to_date(?,'YYYY-MM-DD')"
            );
            preparedStatement.setString(1, source_ic_class);
            preparedStatement.setString(2, ucits_ic_class);
            preparedStatement.setString(3, p_enddate);
            int updateCount = preparedStatement.executeUpdate();
            con.close();
            System.out.println("Nr ic_enddates updated = " + updateCount);
        }

        public static void main(String[] args) throws SQLException, IOException {
            ArrayList recList = new ArrayList();
            ArrayList indexList = new ArrayList();
            if (args.length != 4) {
                System.out.println("Usage: @ucits start_date(dd-mmm-yy) source_pmnem  ucits_ic_class ucits_pmnem");
                System.out.println("  e.g. @ucits 22-mar-15 GLOBMIN UG GLOBMIU");
                return;
            }
            start_date = args[0];
            source_pmnem = args[1].toUpperCase();
            ucits_ic_class = args[2].toUpperCase();
            ucits_pmnem = args[3].toUpperCase();
            System.out.println("Run-time parameters are - Date:" + args[0] + "  Source pmnem:" + source_pmnem + "  UCITS ic_class:" + ucits_ic_class + "  UCITS pmnem:" + ucits_pmnem);

            // Basically 3 main parts plus one optional
            // 1) get original index/portfolio members&weights
            // 2) calculate series of UCITS compliant portfolios based on above
            // 3) Check which UCITS portfolio is the “best” (smallest theta)

            // 4) Store the optimal UCITS portfolio details if required

            get_data(args[0], source_pmnem, recList);
            iterate_weights(recList, indexList);
            get_optimal_pfolio(indexList);
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            System.out.print("Write UCITS data to database? (Y/N): ");
            String s = br.readLine();
            if (s.equals("Y") || s.equals("y"))
                store_optimal_pfolio(index, indexList);
            System.out.println("Finished");
            System.exit(0);
        }
    }
