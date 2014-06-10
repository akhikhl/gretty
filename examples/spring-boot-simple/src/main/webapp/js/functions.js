jQuery.noConflict();

jQuery(function($) {
  $('#result').addClass('hide')
  $('#sendRequest').click(function() {
    $.ajax({ type: 'POST', url: 'mycontroller/getdate', dataType: 'json' }).done(function(data) {
      $('#result').removeClass('hide bg-danger').addClass('bg-success').text('Got from server: ' + data.date);
    }).fail(function(jqXHR, textStatus, errorThrown) {
      $('#result').removeClass('hide bg-success').addClass('bg-danger').text('error: ' + errorThrown);
    });
  });
});
