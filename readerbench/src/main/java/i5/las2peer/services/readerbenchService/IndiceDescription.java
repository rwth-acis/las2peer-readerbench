/**
 * 
 */
package i5.las2peer.services.readerbenchService;
import java.util.*;
/**
 * @author 214
 *
 */
public class IndiceDescription {

	// creating a My HashTable Dictionary
	private Map<String, String> indices = new HashMap<>();
	/**
	 * 
	 */
	public IndiceDescription() {
		// TODO Auto-generated constructor stub
		this.indices.put("AvgDocWdEntropy", "Vielfalt der Konzepte");
		this.indices.put("AvgWordWdLen", "durchschnittlichen Länge aller Wörter");
		this.indices.put("AvgDocAdjCoh_DOC", "Kohäsion zwischen Absätzen, basierend auf word2vec");
		this.indices.put("AvgBlockNoSent", "durchschnittlichen Anzahl von Sätzen pro Absatz");
		this.indices.put("AvgBlockNoWd", "durchschnittlichen Anzahl von Wörtern pro Absatz");
		this.indices.put("AvgBlockPOSMain_noun", "durchschnittlichen Anzahl von Substantiven pro Absatz");
		this.indices.put("AvgBlockPOSMain_verb", "durchschnittlichen Anzahl von Verben pro Absatz");
		this.indices.put("AvgBlockPOSMain_adv", "durchschnittlichen Anzahl von Adverbien pro Absatz");
		this.indices.put("AvgBlockPOSMain_adj", "durchschnittlichen Anzahl von Adjektiven pro Absatz");
		this.indices.put("AvgSentNoWd", "durchschnittlichen Anzahl von Wörtern pro Satz");
		this.indices.put("AvgBlockIntraCoh_BLOCK", "Intra-Absatz-Zusammenhalt, basierend auf word2vec");
		this.indices.put("AvgSentPOSMain_noun", "durchschnittlichen Anzahl von Substantiven pro Satz");
		this.indices.put("AvgSentPOSMain_verb", "durchschnittlichen Anzahl von Verben pro Satz");
		this.indices.put("AvgSentPOSMain_adv", "durchschnittlichen Anzahl von Adverbien pro Satz");
		this.indices.put("AvgSentPOSMain_adj", "durchschnittlichen Anzahl von Adjektiven pro Satz");
		this.indices.put("AvgSentRepetitions", "Wiederholung von Begriffen in Sätzen");
		this.indices.put("AvgSentNoPunct", "durchschnittlichen Anzahl von Satzzeichen auf Satzebene");
		this.indices.put("AvgBlockNoPunct", "durchschnittlichen Anzahl von Satzzeichen auf Absatzebene");
		this.indices.put("AvgSentNoCommas", "durchschnittlichen Anzahl von Kommas auf Satzebene");
		this.indices.put("AvgBlockNoCommas", "durchschnittlichen Anzahl von Kommas auf Absatzebene");
		this.indices.put("PC1", "komplexität der Koncepte");
		this.indices.put("PC2", "Entropie der Konzepte");
		this.indices.put("PC3", "Zusammenhalt/KohÃ¤sion der Absätzen");
		this.indices.put("AvgBlockNoSent", "Anzahl der Sätze");
		this.indices.put("AvgBlockIntraCoh_BLOCK", "Zusammenhalt/KohÃ¤sion zwischen SÃ¤tzen");
		this.indices.put("AvgBlockNoWd", "Anzahl der Wörter");
		this.indices.put("AvgBlockPOSMain_adv", "Anzahl der Adverbien");
		this.indices.put("AvgBlockPOSMain_adj", "Anzahl der Adjektive");
		this.indices.put("AvgBlockPOSMain_noun", "Anzahl der Substantive");
		this.indices.put("AvgSentNoPunct", "Anzahl der Satzzeichen");
		this.indices.put("AvgSentPOSMain_noun", "Anzahl der Substantive");
		this.indices.put("AvgSentPOSMain_adj", "Anzahl der Adjektive");
		this.indices.put("AvgSentPOSMain_adv", "Anzahl der Adverbien");
		this.indices.put("AvgSentPOSMain_verb", "Anzahl der Verben");
		this.indices.put("AvgSentNoCommas", "Anzahl der Kommas");
		this.indices.put("AvgSentPOSMain_pron", "Die Anzahl der Pronomen");
		
	}
	public Map<String, String> getIndiceMap() {
        return indices;
   }

}
