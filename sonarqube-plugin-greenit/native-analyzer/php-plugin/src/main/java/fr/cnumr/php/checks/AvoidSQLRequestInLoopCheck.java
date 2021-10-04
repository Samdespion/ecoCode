package fr.cnumr.php.checks;

import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.statement.*;
import org.sonar.plugins.php.api.visitors.PHPSubscriptionCheck;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

@Rule(
	key = "S72",
	name = "Developpement",
	description = "Avoid SQL request in loop",
	priority = Priority.MINOR,
	tags = {"bug" }
)

public class AvoidSQLRequestInLoopCheck extends PHPSubscriptionCheck {

	private static final String ERROR_MESSAGE = "Avoid SQL request in loop";
	private static final Pattern RegExSQLCall = Pattern.compile("(mysql(i::|_)query\\s*\\(.*)|(oci_execute\\(.*)");

	@Override
	public List<Tree.Kind> nodesToVisit() {
		return Arrays.asList(Kind.FOR_STATEMENT, Kind.FOREACH_STATEMENT, Kind.DO_WHILE_STATEMENT);
	}

	@Override
	public void visitNode(Tree tree) {
		if(tree.is(Kind.FOR_STATEMENT))
			visitBlockNode((BlockTree) ((ForStatementTree)tree).statements().get(0));

		if(tree.is(Kind.FOREACH_STATEMENT))
			visitBlockNode((BlockTree) ((ForEachStatementTree)tree).statements().get(0));

		if(tree.is(Kind.DO_WHILE_STATEMENT))
			visitBlockNode((BlockTree) ((DoWhileStatementTree)tree).statement());
	}

	private void visitBlockNode(BlockTree block)
	{
		block.statements().forEach(this::VisiteChildNode);
	}

	private void VisiteChildNode(Tree tree) {
		if (tree.is(Kind.EXPRESSION_STATEMENT)) {
			ExpressionStatementTree expressionStatementTree = (ExpressionStatementTree) tree;
			String expression = expressionStatementTree.expression().toString();
			VerifyIfThereIsAError(expression, expressionStatementTree);
		}

		if (tree.is(Kind.IF_STATEMENT)) {
			StatementTree statementTree = ((IfStatementTree) tree).statements().get(0);
			if (statementTree.is(Kind.BLOCK))
				visitBlockNode((BlockTree) statementTree);
		 	else
				VisiteChildNode(statementTree);
		}
	}

	private void VerifyIfThereIsAError(String expression, ExpressionStatementTree expressionStatementTree)
	{
		if (RegExSQLCall.matcher(expression).find())
			context().newIssue(this, expressionStatementTree, ERROR_MESSAGE);
	}

}
