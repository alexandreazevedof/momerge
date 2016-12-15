package momerge;

import edu.cmu.cs.fluid.ir.IRNode;
import edu.cmu.cs.fluid.tree.PropagateUpTree;
import edu.cmu.cs.fluid.version.Version;
import edu.cmu.cs.fluid.version.VersionMarker;
import edu.uwm.cs.molhado.merge.IRTreeMerge;
import edu.uwm.cs.molhado.merge.IRTreeMerge.ConflictInfo;
import edu.uwm.cs.molhado.xml.simple.SimpleXmlParser;
import java.io.File;
import java.util.Vector;
import testnodeapi.momergeui;

/**
 *
 * @author chengt
 */
public class momerge {

	public static void main(String[] args) throws Exception {
		SimpleXmlParser p = new SimpleXmlParser(0);

		if (args[0].equals("-stampid")) {
			IRNode doc = p.parse(new File(args[1]));
			Version vx = Version.getVersion();
			p.writeToFile(new File(args[2]), doc);
			return;
		}

		long startMem = Runtime.getRuntime().totalMemory()
				  - Runtime.getRuntime().freeMemory();
		long t0 = System.currentTimeMillis();
		IRNode root = p.parse(new File(args[0]));
		Version v0 = Version.getVersion();
		SimpleXmlParser.tagNameAttr.addDefineObserver(SimpleXmlParser.changeRecord);
		SimpleXmlParser.attrListAttr.addDefineObserver(SimpleXmlParser.changeRecord);
		SimpleXmlParser.tree.addObserver(SimpleXmlParser.changeRecord);
		PropagateUpTree.attach(SimpleXmlParser.changeRecord, SimpleXmlParser.tree);
		Version.saveVersion(v0);
		long t1 = System.currentTimeMillis();
		p.parse(root, new File(args[1]));
		Version v1 = Version.getVersion();
		long t2 = System.currentTimeMillis();
		Version.saveVersion(v0);
		p.parse(root, new File(args[2]));
		long t3 = System.currentTimeMillis();
		Version v2 = Version.getVersion();

		IRTreeMerge merge = new IRTreeMerge(SimpleXmlParser.tree, SimpleXmlParser.changeRecord,
				  root, new VersionMarker(v1), new VersionMarker(v0), new VersionMarker(v2));

		Version v3 = merge.merge();
		long t4 = System.currentTimeMillis();
		if (v3 == null) {
			Version.saveVersion(v2);
			Vector<ConflictInfo> conflicts = merge.getConflicts();
			for (ConflictInfo info : conflicts) {
				System.out.println(" :" + info.description);
			}
			momergeui ui = new momergeui(v0, v1, v2, root);
			ui.merge();
			ui.setLocationRelativeTo(null);
			ui.setVisible(true);
			v3 = ui.merge();
			if (v3 == null) {
				System.exit(0);
			}
			Version.restoreVersion();
			Version.saveVersion(v3);
			SimpleXmlParser.writeToFile(args[3], root);
			//System.exit(0);
		} else {
			Version.saveVersion(v3);
			SimpleXmlParser.writeToFile(args[3], root);
			long t5 = System.currentTimeMillis();

			long endMem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

			System.out.println("parse: " + (t3 - t0));
			System.out.println("merge: " + (t4 - t3));
			System.out.println("write: " + (t5 - t4));
			System.out.println("total: " + (t5 - t0));

			System.out.println("memory: " + (endMem - startMem));
			//System.out.println(nodeTable.size());
		}
	}
}
