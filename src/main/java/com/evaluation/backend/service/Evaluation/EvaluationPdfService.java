package com.evaluation.backend.service.Evaluation;

import com.evaluation.backend.dto.Evaluation.EvaluationWithRubriquesDTO;
import com.evaluation.backend.dto.Evaluation.RubriqueEvaluationDTO;
import com.evaluation.backend.dto.Question.QuestionWithQualificatifDTO;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EvaluationPdfService {

    private static final BaseColor COLOR_HEADER_BG   = new BaseColor(74, 74, 74);
    private static final BaseColor COLOR_RUBRIQUE_BG = new BaseColor(208, 208, 208);
    private static final BaseColor COLOR_COL_BG      = new BaseColor(232, 232, 232);
    private static final BaseColor COLOR_WHITE        = BaseColor.WHITE;
    private static final BaseColor COLOR_BLACK        = BaseColor.BLACK;
    private static final BaseColor COLOR_BORDER       = new BaseColor(180, 180, 180);

    private static final Font FONT_UBO = FontFactory.getFont(
            FontFactory.HELVETICA_BOLD, 14, COLOR_WHITE);
    private static final Font FONT_HEADER_WHITE = FontFactory.getFont(
            FontFactory.HELVETICA_BOLD, 8, COLOR_WHITE);
    private static final Font FONT_HEADER_BLACK = FontFactory.getFont(
            FontFactory.HELVETICA_BOLD, 8, COLOR_BLACK);
    private static final Font FONT_RUBRIQUE = FontFactory.getFont(
            FontFactory.HELVETICA_BOLD, 8, COLOR_BLACK);
    private static final Font FONT_QUESTION = FontFactory.getFont(
            FontFactory.HELVETICA, 8, COLOR_BLACK);
    private static final Font FONT_QUALIF = FontFactory.getFont(
            FontFactory.HELVETICA_OBLIQUE, 7, COLOR_BLACK);
    private static final Font FONT_META_LABEL = FontFactory.getFont(
            FontFactory.HELVETICA_BOLD, 8, COLOR_BLACK);
    private static final Font FONT_META_VALUE = FontFactory.getFont(
            FontFactory.HELVETICA, 8, COLOR_BLACK);
    private static final Font FONT_FOOTER = FontFactory.getFont(
            FontFactory.HELVETICA, 7, new BaseColor(100, 100, 100));
    private static final Font FONT_FORMATION = FontFactory.getFont(
            FontFactory.HELVETICA_BOLD, 10, COLOR_BLACK);
    private static final Font FONT_HEADER_SUBTITLE = FontFactory.getFont(
            FontFactory.HELVETICA, 7, COLOR_BLACK);
    private static final Font FONT_HEADER_SUBTITLE_BOLD = FontFactory.getFont(
            FontFactory.HELVETICA_BOLD, 7, COLOR_BLACK);

    private static final float HEADER_HEIGHT = 65f;
    private static final float FOOTER_HEIGHT = 20f;
    private static final float MARGIN = 15f * 2.83f;

    public byte[] generateEvaluationPdf(EvaluationWithRubriquesDTO evaluation) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            Document document = new Document(
                    PageSize.A4,
                    MARGIN, MARGIN,
                    MARGIN + HEADER_HEIGHT,
                    MARGIN + FOOTER_HEIGHT
            );

            PdfWriter writer = PdfWriter.getInstance(document, baos);

            writer.setPageEvent(new PdfPageEventHelper() {
                @Override
                public void onEndPage(PdfWriter w, Document doc) {
                    try {
                        drawHeader(w, doc, evaluation);
                        drawFooter(w, doc, evaluation);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            });

            document.open();

            document.add(buildMetaTable(evaluation));
            document.add(Chunk.NEWLINE);
            document.add(buildMainTable(evaluation));

            document.close();
            return baos.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Erreur génération PDF : " + e.getMessage(), e);
        }
    }

    // ── EN-TÊTE ──────────────────────────────────────────────────────────────

    private void drawHeader(PdfWriter writer, Document doc, EvaluationWithRubriquesDTO eval) throws Exception {
        float pageWidth  = doc.getPageSize().getWidth();
        float topY       = doc.getPageSize().getHeight() - MARGIN + 5f;
        float tableWidth = pageWidth - 2 * MARGIN;

        PdfPTable headerTable = new PdfPTable(new float[]{65, 350, 65});
        headerTable.setTotalWidth(tableWidth);
        headerTable.setLockedWidth(true);

        // Cellule UBO
        PdfPCell uboCell = new PdfPCell(new Phrase("UBO", FONT_UBO));
        uboCell.setBackgroundColor(new BaseColor(60, 60, 60));
        uboCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        uboCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        uboCell.setPadding(6);
        uboCell.setBorderColor(COLOR_BORDER);
        headerTable.addCell(uboCell);

        // Cellule titre central
        Phrase titlePhrase = new Phrase();
        titlePhrase.add(new Chunk("Ingénierie des Systèmes d'Information\n", FONT_HEADER_SUBTITLE_BOLD));
        titlePhrase.add(new Chunk("Système d'Information SPI – Evaluation des Enseignements\n", FONT_HEADER_SUBTITLE));
        titlePhrase.add(new Chunk("Dossier de Spécification des Exigences", FONT_HEADER_SUBTITLE));
        PdfPCell titleCell = new PdfPCell(titlePhrase);
        titleCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        titleCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        titleCell.setPadding(5);
        titleCell.setBorderColor(COLOR_BORDER);
        headerTable.addCell(titleCell);

        // Cellule année
        PdfPCell anneeCell = new PdfPCell(
                new Phrase(eval.getAnneeUniversitaire() != null ? eval.getAnneeUniversitaire() : "", FONT_HEADER_BLACK));
        anneeCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        anneeCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        anneeCell.setPadding(5);
        anneeCell.setBorderColor(COLOR_BORDER);
        headerTable.addCell(anneeCell);

        // Ligne sous-titre
        String sousTitre = (eval.getCodeFormation() != null ? eval.getCodeFormation() : "")
                + "    Evaluation d'un enseignement"
                + (eval.getDesignation() != null ? " — " + eval.getDesignation() : "");
        PdfPCell formCell = new PdfPCell(new Phrase(sousTitre, FONT_FORMATION));
        formCell.setColspan(3);
        formCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        formCell.setPadding(5);
        formCell.setBorder(Rectangle.BOTTOM);
        formCell.setBorderColor(COLOR_BORDER);
        headerTable.addCell(formCell);

        headerTable.writeSelectedRows(0, -1, MARGIN, topY, writer.getDirectContent());
    }

    // ── PIED DE PAGE ─────────────────────────────────────────────────────────

    private void drawFooter(PdfWriter writer, Document doc, EvaluationWithRubriquesDTO eval) throws Exception {
        float pageWidth  = doc.getPageSize().getWidth();
        float bottomY    = MARGIN - 5f;
        float tableWidth = pageWidth - 2 * MARGIN;

        PdfPTable footerTable = new PdfPTable(new float[]{1, 1});
        footerTable.setTotalWidth(tableWidth);
        footerTable.setLockedWidth(true);

        PdfPCell leftCell = new PdfPCell(new Phrase("Philippe.Saliou@univ-brest.fr", FONT_FOOTER));
        leftCell.setBorder(Rectangle.TOP);
        leftCell.setBorderColor(COLOR_BORDER);
        leftCell.setPaddingTop(3);
        leftCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        footerTable.addCell(leftCell);

        String dateStr = eval.getFinReponse() != null ? eval.getFinReponse().toString() : "";
        PdfPCell rightCell = new PdfPCell(new Phrase(dateStr, FONT_FOOTER));
        rightCell.setBorder(Rectangle.TOP);
        rightCell.setBorderColor(COLOR_BORDER);
        rightCell.setPaddingTop(3);
        rightCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        footerTable.addCell(rightCell);

        footerTable.writeSelectedRows(0, -1, MARGIN, bottomY, writer.getDirectContent());
    }

    // ── TABLEAU MÉTADONNÉES ───────────────────────────────────────────────────

    private PdfPTable buildMetaTable(EvaluationWithRubriquesDTO eval) throws Exception {
        PdfPTable table = new PdfPTable(new float[]{110, 140, 110, 140});
        table.setWidthPercentage(100);
        table.setSpacingAfter(6f);

        addMetaRow(table,
                "Unité d'Enseignement", eval.getCodeUe(),
                "Période",              eval.getPeriode());
        addMetaRow(table,
                "Elément Constitutif",  eval.getCodeEc() != null ? eval.getCodeEc() : "",
                "Année universitaire",  eval.getAnneeUniversitaire());

        return table;
    }

    private void addMetaRow(PdfPTable t, String l1, String v1, String l2, String v2) {
        for (String[] pair : new String[][]{{l1, v1}, {l2, v2}}) {
            PdfPCell lc = new PdfPCell(new Phrase(pair[0], FONT_META_LABEL));
            lc.setPadding(3); lc.setBorderColor(COLOR_BORDER);
            t.addCell(lc);
            PdfPCell vc = new PdfPCell(new Phrase(pair[1] != null ? pair[1] : "", FONT_META_VALUE));
            vc.setPadding(3); vc.setBorderColor(COLOR_BORDER);
            t.addCell(vc);
        }
    }

    // ── TABLEAU PRINCIPAL ─────────────────────────────────────────────────────
    // Colonnes : question(0) | min(1) | 1(2) | 2(3) | 3(4) | 4(5) | 5(6) | max(7)

    private PdfPTable buildMainTable(EvaluationWithRubriquesDTO eval) throws Exception {
        PdfPTable table = new PdfPTable(new float[]{180, 55, 22, 22, 22, 22, 22, 55});
        table.setWidthPercentage(100);
        table.setHeaderRows(2); // répété sur chaque page

        // ── Ligne 1 : Minimum(col 1+2), Moyen(col 3+4), Maximum(col 5+max) ──
        addHCellColspan(table, "",        2, COLOR_HEADER_BG, FONT_HEADER_WHITE, Element.ALIGN_CENTER);
        addHCellColspan(table, "Minimum", 2, COLOR_HEADER_BG, FONT_HEADER_WHITE, Element.ALIGN_CENTER);
        addHCellColspan(table, "Moyen",   2, COLOR_HEADER_BG, FONT_HEADER_WHITE, Element.ALIGN_CENTER);
        addHCellColspan(table, "Maximum", 2, COLOR_HEADER_BG, FONT_HEADER_WHITE, Element.ALIGN_CENTER);

        // ── Ligne 2 : chiffres 1-5 ──
        addHCellColspan(table, "",  1, COLOR_COL_BG, FONT_HEADER_BLACK, Element.ALIGN_CENTER);
        addHCellColspan(table, "",  1, COLOR_COL_BG, FONT_HEADER_BLACK, Element.ALIGN_CENTER);
        addHCellColspan(table, "1", 1, COLOR_COL_BG, FONT_HEADER_BLACK, Element.ALIGN_CENTER);
        addHCellColspan(table, "2", 1, COLOR_COL_BG, FONT_HEADER_BLACK, Element.ALIGN_CENTER);
        addHCellColspan(table, "3", 1, COLOR_COL_BG, FONT_HEADER_BLACK, Element.ALIGN_CENTER);
        addHCellColspan(table, "4", 1, COLOR_COL_BG, FONT_HEADER_BLACK, Element.ALIGN_CENTER);
        addHCellColspan(table, "5", 1, COLOR_COL_BG, FONT_HEADER_BLACK, Element.ALIGN_CENTER);
        addHCellColspan(table, "",  1, COLOR_COL_BG, FONT_HEADER_BLACK, Element.ALIGN_CENTER);

        // ── Rubriques + questions ──
        for (RubriqueEvaluationDTO rubrique : eval.getRubriques()) {
            addRubriqueRow(table, rubrique.getDesignation());
            List<QuestionWithQualificatifDTO> questions = rubrique.getQuestions();
            if (questions != null) {
                for (QuestionWithQualificatifDTO q : questions) {
                    addQuestionRow(table, q);
                }
            }
        }

        // ── Commentaires : une seule grande cellule ──
        addRubriqueRow(table, "Commentaires");
        PdfPCell commentCell = new PdfPCell(new Phrase(""));
        commentCell.setColspan(8);
        commentCell.setMinimumHeight(80f);
        commentCell.setBorderColor(COLOR_BORDER);
        table.addCell(commentCell);

        return table;
    }

    // ── MÉTHODES UTILITAIRES ──────────────────────────────────────────────────

    private void addHCellColspan(PdfPTable table, String text, int colspan, BaseColor bg, Font font, int align) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(bg);
        cell.setColspan(colspan);
        cell.setHorizontalAlignment(align);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(3);
        cell.setBorderColor(COLOR_BORDER);
        table.addCell(cell);
    }

    private void addRubriqueRow(PdfPTable table, String designation) {
        PdfPCell nameCell = new PdfPCell(new Phrase(designation != null ? designation : "", FONT_RUBRIQUE));
        nameCell.setBackgroundColor(COLOR_RUBRIQUE_BG);
        nameCell.setPadding(4);
        nameCell.setBorderColor(COLOR_BORDER);
        table.addCell(nameCell);

        PdfPCell emptyMin = new PdfPCell(new Phrase(""));
        emptyMin.setBackgroundColor(COLOR_RUBRIQUE_BG);
        emptyMin.setPadding(4);
        emptyMin.setBorderColor(COLOR_BORDER);
        table.addCell(emptyMin);

        for (int i = 1; i <= 5; i++) {
            PdfPCell numCell = new PdfPCell(new Phrase(String.valueOf(i), FONT_HEADER_BLACK));
            numCell.setBackgroundColor(COLOR_RUBRIQUE_BG);
            numCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            numCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            numCell.setPadding(4);
            numCell.setBorderColor(COLOR_BORDER);
            table.addCell(numCell);
        }

        PdfPCell emptyMax = new PdfPCell(new Phrase(""));
        emptyMax.setBackgroundColor(COLOR_RUBRIQUE_BG);
        emptyMax.setPadding(4);
        emptyMax.setBorderColor(COLOR_BORDER);
        table.addCell(emptyMax);
    }

    private void addQuestionRow(PdfPTable table, QuestionWithQualificatifDTO q) {
        PdfPCell qCell = new PdfPCell(new Phrase(q.getIntitule() != null ? q.getIntitule() : "", FONT_QUESTION));
        qCell.setPadding(3);
        qCell.setMinimumHeight(14f);
        qCell.setBorderColor(COLOR_BORDER);
        table.addCell(qCell);

        PdfPCell minCell = new PdfPCell(new Phrase(q.getMinimal() != null ? q.getMinimal() : "", FONT_QUALIF));
        minCell.setPadding(3);
        minCell.setBorderColor(COLOR_BORDER);
        table.addCell(minCell);

        for (int i = 0; i < 5; i++) {
            PdfPCell numCell = new PdfPCell(new Phrase(""));
            numCell.setBackgroundColor(new BaseColor(248, 248, 248));
            numCell.setMinimumHeight(14f);
            numCell.setPadding(3);
            numCell.setBorderColor(COLOR_BORDER);
            table.addCell(numCell);
        }

        PdfPCell maxCell = new PdfPCell(new Phrase(q.getMaximal() != null ? q.getMaximal() : "", FONT_QUALIF));
        maxCell.setPadding(3);
        maxCell.setBorderColor(COLOR_BORDER);
        table.addCell(maxCell);
    }
}