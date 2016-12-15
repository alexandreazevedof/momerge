package momerge;

import edu.cmu.cs.fluid.ir.IRNode;
import edu.cmu.cs.fluid.tree.PropagateUpTree;
import edu.cmu.cs.fluid.version.Version;
import edu.cmu.cs.fluid.version.VersionMarker;
import edu.uwm.cs.molhado.merge.IRTreeMerge;
import edu.uwm.cs.molhado.merge.IRTreeMerge.ConflictInfo;
import edu.uwm.cs.molhado.merge.XmlDocMerge;
import edu.uwm.cs.molhado.xml.simple.SimpleXmlParser3;
import java.io.File;
import java.util.Vector;
import testnodeapi.momergeui;

/**
 *
 * Code to test SimpleXmlParser3 which is updated to use IRSequence instead of
 * a Java collection. Reason for using IRSequence is so that we can store the
 * tree.  We only use Java collection for children to speed up merging but
 * normal merging of files do not need to store the IR.
 * 
 * @author chengt
 */
public class momerge2 {

	public static void main(String[] args) throws Exception {
		SimpleXmlParser3 p = new SimpleXmlParser3(0, SimpleXmlParser3.VDOC_PARSING);
//
//		if (args[0].equals("-stampid")){
//			IRNode doc = p.parse(new File(args[1]));
//			Version vx = Version.getVersion();
//			p.writeToFile(new File(args[2]), doc);
//			return;
//		}

		long startMem = Runtime.getRuntime().totalMemory()
				  - Runtime.getRuntime().freeMemory();
		long t0 = System.currentTimeMillis();
		//IRNode root = p.parse(new File(args[0]));
		IRNode root = p.parse("<a molhado:id='10'><b molhado:id='11'/><c molhado:id='25'/><d molhado:id='3'/></a>");
		System.out.println("Base document");
		System.out.println(SimpleXmlParser3.toStringWithID(root));
		Version v0 = Version.getVersion();

		if (true) System.exit(0);

		//Version.saveVersion(v0);
		Version.setVersion(v0);
		long t1 = System.currentTimeMillis();
		System.out.println("Document A");
		p.parse(root, "<a molhado:id='0'><b molhado:id='1'><x molhado:id='4' /></b><c molhado:id='2'/><d molhado:id='3'/></a>");
		System.out.println(SimpleXmlParser3.toStringWithID(root));
		Version v1 = Version.getVersion();
		long t2 = System.currentTimeMillis();

		System.out.println("Document B");
		//Version.saveVersion(v0);
		Version.setVersion(v0);
		p.parse(root, "<a molhado:id='0'><b molhado:id='1'/><c molhado:id='2'><y molhado:id='4'/></c><d molhado:id='3'/></a>");
		System.out.println(SimpleXmlParser3.toStringWithID(root));
		long t3 = System.currentTimeMillis();
		Version v2 = Version.getVersion();

		XmlDocMerge merge = new XmlDocMerge(SimpleXmlParser3.tree, SimpleXmlParser3.changeRecord,
				  root, new VersionMarker(v1), new VersionMarker(v0), new VersionMarker(v2));

		Version v3 = merge.merge();
		long t4 = System.currentTimeMillis();
		if (v3 == null) {
			Version.saveVersion(v2);
			Vector<XmlDocMerge.ConflictInfo> conflicts = merge.getConflicts();
			for (XmlDocMerge.ConflictInfo info : conflicts) {
				System.out.println(" :" + info.description);
			}
//			momergeui ui = new momergeui(v0, v1, v2, root);
//			ui.merge();
//			ui.setLocationRelativeTo(null);
//			ui.setVisible(true);
//			v3 = ui.merge();
//			if (v3 == null) System.exit(0);
//      Version.restoreVersion();
//			Version.saveVersion(v3);
//			SimpleXmlParser3.writeToFile(args[3],root);
			//System.exit(0);
		} else {
			Version.saveVersion(v3);
			//SimpleXmlParser3.writeToFile(args[3],root);
			System.out.println("Merge result=");
			System.out.println(SimpleXmlParser3.toStringWithID(root));
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
